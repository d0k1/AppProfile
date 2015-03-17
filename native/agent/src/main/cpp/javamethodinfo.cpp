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

#include "javamethodinfo.h"
#include "utils.h"
JavaMethodInfo::JavaMethodInfo(unsigned int classIndex, unsigned int methodIndex, const char *name, const char *signature, unsigned long long methodId, JavaClassInfo *info):classIndex(classIndex), methodIndex(methodIndex),name(name),signature(signature),methodCounter(methodId),classInfo(info){
  methodId = Utils::getMethodId(classIndex, methodIndex);
}

JavaMethodInfo::JavaMethodInfo(unsigned int classIndex, unsigned int methodIndex, string name, string signature, unsigned long long methodId, JavaClassInfo *info):classIndex(classIndex), methodIndex(methodIndex),name(name),signature(signature),methodCounter(methodId),classInfo(info){
  methodId = Utils::getMethodId(classIndex, methodIndex);
}

unsigned long long JavaMethodInfo::getMethodId(){
  return methodId;
}

JavaClassInfo *JavaMethodInfo::getClass(){
  return classInfo;
}

string JavaMethodInfo::getFQN(){
  string methodName(getClass()->getName());
  methodName+="#";
  methodName+=getName();
  methodName+=getSignature();
  
  return methodName;
}