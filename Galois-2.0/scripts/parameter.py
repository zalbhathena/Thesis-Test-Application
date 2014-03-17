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

import galois

PARAM_OPT = '-p'

STATS = 'stats.txt'
MERGED_STATS = 'parameterStats.csv'
VISUALOUT = 'parameterProfile.pdf'

UNIX_R = 'Rscript'
WIN_R = 'Rscript.exe'
# relative path to R plotting script
R_SCRIPT_PATH = 'scripts/parameter.R'

PATTERN_MERGED_STATS = r'\= Merged Statistics \='
PATTERN_BEGIN_STATS = r'\= Begin Parameter Statistics \='
PATTERN_END_STATS = r'\= End Parameter Statistics \='

def die(msg):
  """
  exit with msg
  """
  sys.stderr.write('%s\n'%msg)
  sys.exit(-1)

def runParaMeter(runConf):
  """
  runs java cmd for ParaMeter
  baseDir is Galois root dir
  """
  vmargs = runConf.getVMargs()
  clspath = runConf.getClassPath()
  gopts = runConf.getGaloisOpts()
  gopts.append(PARAM_OPT)
  
  runConf.runJava(vmargs=vmargs, clspath=clspath, gopts=gopts, args=runConf.args)

def getRpath(runConf):
  """
  tries to find if R is present on the path
  or if R_PATH is defined
  """
  Rpath = None
  if runConf.options.Rpath:
    Rpath = runConf.options.Rpath
    galois.debug('found in cmdline = %s' % Rpath)
  elif os.environ.has_key('R_PATH'):
    Rpath = os.environ['R_PATH']
    galois.debug('found in environment R_PATH = %s' % Rpath)

  if Rpath != None and os.access(Rpath, os.X_OK):
    return Rpath
 
  for p in [UNIX_R, WIN_R]:
    testcmd = '%s --version' % p
    galois.debug('R testcmd: %s' % testcmd)
    try:
      subprocess.check_call(shlex.split(testcmd))
    except subprocess.CalledProcessError:
      galois.debug('caught exception')
      continue
    except OSError:
      galois.debug('caught exception')
      continue
    else:
      return p

  return None

  
def runR(runConf):
  """
  reads the parameter generated STATS file and generates
  a new file MERGED_STATS to run R on it
  """

  statsFile = open(STATS, 'r')
  csvFile = None

  foundMerged = False
  foundBegin = False
  for l in statsFile:
    if not foundBegin:
      if not foundMerged and re.search(PATTERN_MERGED_STATS, l):
        foundMerged = True
      if foundMerged and re.search(PATTERN_BEGIN_STATS, l):
        foundBegin = True
        csvFile = open(MERGED_STATS, 'w')
    else:
      if re.search(PATTERN_END_STATS,l):
        csvFile.close()
        break
      else:
        csvFile.write(l)
  else:
    die('could not find merged parameter stats in %s'%STATS)


  Rpath = getRpath(runConf)
  if Rpath:
    rScript = os.path.join(runConf.baseDir, R_SCRIPT_PATH)
    rcmd = '%s %s "%s" "%s"'%(Rpath, rScript, VISUALOUT, MERGED_STATS)
    galois.debug('executing R with cmd: %s' % rcmd)
    subprocess.check_call(shlex.split(rcmd))
  else:
    print '''
    could not find R satistical tool, please do one of the following:
      define environment variable R_PATH
      add R to standard path
      use --help to see how to pass R on cmd line
    ParaMeter text results are in %s
    ''' % MERGED_STATS



def main():
  baseDir = galois.getBaseDir()

  galoisOpts = {}
  custom_options = [
    optparse.make_option('--Rpath', dest='Rpath', action='store', default=UNIX_R, 
      type='string', help='provide the path to R statistical and plotting tool')
  ]

  (options, args) = galois.parseArgs(galoisOpts, custom_options)
  runConf = galois.RunConfig(baseDir, options, galoisOpts, args)

  runParaMeter(runConf)
  runR(runConf)


if __name__ == '__main__':
  main()
