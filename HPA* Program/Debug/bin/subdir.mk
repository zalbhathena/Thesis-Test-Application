################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C++_SRCS += \
../bin/HPAProgram.c++ 

C++_DEPS += \
./bin/HPAProgram.d 

OBJS += \
./bin/HPAProgram.o 


# Each subdirectory must supply rules for building sources it contributes
bin/%.o: ../bin/%.c++
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


