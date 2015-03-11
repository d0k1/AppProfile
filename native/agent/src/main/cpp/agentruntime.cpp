/*
 * <one line to give the program's name and a brief idea of what it does.>
 * Copyright (C) 2015  <copyright holder> <email>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#include "agentruntime.h"
#include <iostream>
#include <sys/types.h>
#include <unistd.h>
#include <sys/syscall.h>

AgentOptions *AgentRuntime::getOptions(){
  return options;
}

string AgentRuntime::getEnvVariable(string name)
{
  agentGlobalLock();
  char *value;
  jvmtiError error = ( jvmti )->GetSystemProperty(name.c_str(), &value);
  JVMTIExitIfError(error, "Cannot get environment value" );
  string result(value);
  JVMTIFree(value);
  agentGlobalUnlock();
  return result;
}

void AgentRuntime::VmStarted(){
  vm_started = true;
}

void AgentRuntime::VmDead(){
  vm_dead = true;
}

bool AgentRuntime::isVmStarted(){
  return vm_started;
}

bool AgentRuntime::isVmDead(){
  return vm_dead;
}

void AgentRuntime::agentGlobalLock(){
  jvmtiError error;
  
  error = ( jvmti )->RawMonitorEnter ( lock );
  JVMTIExitIfError(error, "Cannot enter with raw monitor" );
}

void AgentRuntime::agentGlobalUnlock(){
  jvmtiError error;
  
  error = ( jvmti )->RawMonitorExit ( lock );
  JVMTIExitIfError(error, "Cannot exit with raw monitor" );
}

AgentRuntime::AgentRuntime( jvmtiEnv* jvmti ):jvmti(jvmti),vm_dead(false),vm_started(false) {
  auto error = ( jvmti )->CreateRawMonitor ( "agentGlobalLock", & ( lock ) );
  
  JVMTIExitIfError(error, "Cannot create raw monitor" );  
  
  string propertiesFile = getEnvVariable("agent.config");
  
  options = new AgentOptions(propertiesFile);
}

JavaThreadInfo AgentRuntime::getThreadInfo(jthread thread){
  jvmtiThreadInfo info;
  string name;
  
  /* Get the thread information, which includes the name */
  jvmtiError error = ( jvmti )->GetThreadInfo ( thread, &info );
  JVMTIExitIfError(error, "Cannot get thread info" );
  
  if ( info.name != nullptr ) {
    name = info.name;
    JVMTIFree(( void* ) info.name );
  }
  
  auto ptid = pthread_self();
  auto stid = syscall(__NR_gettid);
  
  return JavaThreadInfo(name, stid, ptid);
}

JavaThreadInfo AgentRuntime::getCurrentThreadInfo(){
  auto ptid = pthread_self();
  auto stid = -1;//(pid_t)syscall(__NR_gettid);
  
  return JavaThreadInfo(stid, ptid);
}

void *AgentRuntime::JVMTIAllocate(int len){
  jvmtiError error;
  void      *ptr;
  
  error = ( jvmti )->Allocate ( len, ( unsigned char ** ) &ptr );
  JVMTIExitIfError (error, "Cannot allocate memory" );
  return ptr;
}

void AgentRuntime::JVMTIFree(void *ptr){
  jvmtiError error;
  
  error = ( jvmti )->Deallocate ( ( unsigned char * ) ptr );
  JVMTIExitIfError ( error, "Cannot deallocate memory" );
}

void AgentRuntime::JVMTIExitIfError(jvmtiError errnum, const char *str){
  if ( errnum != JVMTI_ERROR_NONE ) {
    char       *errnum_str;
    
    errnum_str = NULL;
    ( void ) ( jvmti )->GetErrorName ( errnum, &errnum_str );
    
    fatal_error ( "ERROR: JVMTI: %d(%s): %s\n", errnum,
		  ( errnum_str==NULL?"Unknown":errnum_str ),
		  ( str==NULL?"":str ) );
  }
}

void AgentRuntime::JVMTIAddJarToClasspath(const char *name){
  jvmtiError error;
  error = ( jvmti )->AddToBootstrapClassLoaderSearch ( ( const char* ) name );
  JVMTIExitIfError ( error, "Cannot add to boot classpath" );
  
  error = ( jvmti )->AddToBootstrapClassLoaderSearch ( ( const char* ) name );
  JVMTIExitIfError ( error, "Cannot add to boot classpath" );
}

void fatal_error ( const char * format, ... ) {
  va_list ap;
  
  va_start ( ap, format );
  ( void ) vfprintf ( stderr, format, ap );
  ( void ) fflush ( stderr );
  va_end ( ap );
  exit ( 3 );
}

