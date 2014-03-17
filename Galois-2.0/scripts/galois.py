#!/usr/bin/env python
# Galois, a framework to exploit amorphous data-parallelism in irregular
# programs.
# 
# Copyright (C) 2010, The University of Texas at Austin. All rights reserved.
# UNIVERSITY EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES CONCERNING THIS SOFTWARE
# AND DOCUMENTATION, INCLUDING ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR ANY
# PARTICULAR PURPOSE, NON-INFRINGEMENT AND WARRANTIES OF PERFORMANCE, AND ANY
# WARRANTY THAT MIGHT OTHERWISE ARISE FROM COURSE OF DEALING OR USAGE OF TRADE.
# NO WARRANTY IS EITHER EXPRESS OR IMPLIED WITH RESPECT TO THE USE OF THE
# SOFTWARE OR DOCUMENTATION. Under no circumstances shall University be liable
# for incidental, special, indirect, direct or consequential damages or loss of
# profits, interruption of business, or related expenses which may arise from use
# of Software or Documentation, including but not limited to those resulting from
# defects in Software and/or Documentation, or loss or inaccuracy of data of any
# kind.
# 
# 

import sys
import os
import re
import subprocess
import optparse
import shlex
import signal


LIBDIR = 'lib'
CLASSDIR = 'classes'
GALOIS_RUNTIME = 'galois.runtime.GaloisRuntime'

DEBUG = False

def debug(msg):
  if DEBUG:
    print '>>> %s'%msg


def parseArgs(galoisOpts, custom_options=[]):
  """
  params:
    galoisOpts map
    fills up the map galoisOpts with (key,val) pairs where keys are 
    the options passed to GALOIS_RUNTIME and values are optparse.Option

    custom_options
      parameter.py uses it to pass some tool specific options e.g. --Rpath
  returns:
    tuple containing options and remaining command line args
  """

  galoisOpts['-r'] = optparse.make_option('-r', dest='runs', action='store', default=1, type="int",
                    help='use RUNS runs in the same vm', metavar='RUNS')
  galoisOpts['-t'] = optparse.make_option('-t', dest='threads', action='store', default=1, type="int",
                    help='use number of threads=THREADS', metavar='THREADS')
  galoisOpts['-f'] = optparse.make_option('-f', dest='prop', action='store',
                    help='read app arguments from properties file', metavar='PROP')
  galoisOpts['-g'] = optparse.make_option('-g', dest='profile', action='store_true', default=False,
                    help='perform profiling of the runtime')
  galoisOpts['-s'] = optparse.make_option('-s', dest='useSerial', action='store_true', default=False,
                    help='use serial runtime')
  galoisOpts['-i'] = optparse.make_option('-i', dest='ignoreFlags', action='store_true', default=False,
                    help='unsafe option')
  galoisOpts['--dr'] = optparse.make_option('--dr', dest='recordReplay', action='store_true', default=False,
                    help='unsafe option')
  galoisOpts['--dp'] = optparse.make_option('--dp', dest='playbackReplay', action='store_true', default=False,
                    help='unsafe option')
  
  option_list = galoisOpts.values()
  option_list.extend(custom_options)
  parser = optparse.OptionParser(
      option_list=option_list,
      usage='usage: %prog [options] [--] [class] [args]')

  parser.add_option('--vm', dest='vmopts', action='append', default=[],
                    help='pass VMOPT to java', metavar='VMOPT')
  parser.add_option('-m', dest='mem', action='store', default='', type='string',
                    help='use HEAPSIZE memory', metavar='HEAPSIZE')
  parser.add_option('-d', dest='da', action='store_true', default=False,
                    help='disable assertions')
  parser.add_option('--verbose', dest='DEBUG', action='store_true', default=False,
                    help='enable debug prints')

  (options, args) = parser.parse_args()

  if not args:
    parser.error('need classname to run')

  # set the debug flag
  if options.DEBUG:
    global DEBUG
    DEBUG = True

  return (options, args)


class RunConfig:
  """
  sets up a config of vm args, GALOIS_RUNTIME options etc to execute
  the java command
  """

  def __init__(self, baseDir, options, galoisOpts, args):
    self.baseDir = baseDir
    self.options = options
    self.galoisOpts = galoisOpts
    self.args = args


  def getClassPath(self):
    """
    sets up LD_LIBRARY_PATH
    returns a list containing all the jar files in baseDir/LIBDIR
    """
    debug('base path: %s'%self.baseDir)

    libPath = os.path.join(self.baseDir, LIBDIR)

    # setup LD_LIBRARY_PATH
    if os.environ.has_key('LD_LIBRARY_PATH'): 
      os.environ['LD_LIBRARY_PATH'] = '%s:%s'%(os.environ['LD_LIBRARY_PATH'], libPath)
    else:
      os.environ['LD_LIBRARY_PATH'] = libPath

    debug('libPath: %s'%libPath)

    # libFiles = []
    # for root, dirs, files in os.walk(libPath):
      # for f in files:
        # if re.search(r'.jar$', f, re.IGNORECASE):
          # libFiles.append(os.path.abspath(f))

    libFiles = [os.path.join(libPath,f) for f in os.listdir(libPath) if re.search(r'.jar$',f)]

    libFiles.append( os.path.join(self.baseDir, CLASSDIR))
    
    debug('classpath: %s'%libFiles)
    return libFiles


  def getGaloisOpts(self):
    """
    returns a list of options that need to be passed as arguments to GALOIS_RUNTIME
    example return val could look like ['-t', '2', '-r', '3', '-g']
    """
    gopts = []
    for (k,v) in self.galoisOpts.iteritems():
      if hasattr(self.options, v.dest):
        optarg = getattr(self.options, v.dest)
        if optarg:
          gopts.append(k)
          if type(optarg) != type(bool()):
            gopts.append(str(optarg))

    return gopts


  def getVMargs(self):
    """
    bundles up the Java VM args into a list
    """
    ea = '-ea'
    if self.options.da:
      ea = '-da'

    # XXX(ddn) 5/25/10. As of Java5/6 -XX:+UseParallelGC is enabled for
    # most machines > 2 CPUs, but parallel GC of oldspace is still
    # not on. Enable both to be sure. Set ratio of new to old
    # (default 2 on "servers") to favor a larger nursery because
    # concurrent Java code generates a lot of short-lived garbage.
    vmargs = ['-XX:+UseParallelGC', '-XX:+UseParallelOldGC', '-XX:NewRatio=1']
    vmargs.append(ea)

    if self.options.mem:
      vmargs.extend( ['-Xms%s'%self.options.mem, '-Xmx%s'%self.options.mem] )

    vmargs.extend( self.options.vmopts )
    return vmargs

  def runJava(self, vmargs, clspath, gopts, args):
    """
    params:
      vmargs: list containing vmargs 
      clspath: a list containing Java classpath elements
      gopts: list of options (with their args) to be passed to GALOIS_RUNTIME
      args: remaining command line arguments containing main class and args
    """
    vmargsStr = ' '.join(vmargs)
    clspathStr = ':'.join(clspath)
    goptsStr = ' '.join(gopts)
    argsStr = ' '.join(args)
    cmd = 'java %s -cp %s %s %s %s' % (vmargsStr, clspathStr, GALOIS_RUNTIME, goptsStr, argsStr)
    debug('executing: %s' % cmd)
    retcode = subprocess.call(shlex.split(cmd)) #, shell=True)
    if retcode != 0:
      # XXX(ddn): Strange behavior where last of subprocess java command is
      # truncated sys.stderr.flush() doesn't fix it but sys.stderr.write("")
      # does...
      sys.stderr.write("")
      sys.stderr.write("Error running command: %s\n" % cmd)
      sys.stderr.flush()
      sys.exit(1)


  def run(self):
    clspath = self.getClassPath()
    vmargs = self.getVMargs()
    gopts = self.getGaloisOpts()

    debug('galoisOpts: %s'% gopts)
    self.runJava(vmargs, clspath, gopts, self.args)



def getBaseDir():
  baseDir = os.path.join(os.path.dirname(sys.argv[0]), os.pardir)
  baseDir = os.path.normpath(baseDir)
  return baseDir



def main():
  baseDir = getBaseDir()
  galoisOpts = {}
  (options, args) = parseArgs(galoisOpts)

  runConf = RunConfig(baseDir, options, galoisOpts, args)

  debug('remaining args = %s'%args)

  runConf.run()



if __name__ == '__main__':
  signal.signal(signal.SIGQUIT, signal.SIG_IGN)
  main()
