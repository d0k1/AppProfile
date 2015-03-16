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

#include "javaclassinfo.h"

#include <iostream>

using namespace std;

JavaClassInfo::JavaClassInfo(unsigned int classIndex, string name){
  this->name = name;
  this->classIndex = classIndex;
}

JavaClassInfo::JavaClassInfo(unsigned int classIndex, const char *name){
  this->name = name;
  this->classIndex = classIndex;
}

unsigned int JavaClassInfo::getMethodCount(){
    return methods.size();
}

JavaMethodInfo *JavaClassInfo::addMethod(unsigned int methodIndex, string name, string signature, unsigned long methodId) {
  JavaMethodInfo *info = new JavaMethodInfo(classIndex, methodIndex, name, signature, methodId, this);
  methods.push_back(info);
  return info;
}

JavaMethodInfo *JavaClassInfo::addMethod(unsigned int methodIndex, const char *name, const char *signature, unsigned long methodId) {
  JavaMethodInfo *info = new JavaMethodInfo(classIndex, methodIndex, name, signature, methodId, this);
  methods.push_back(info);
  
  return info;
}

JavaMethodInfo *JavaClassInfo::getMethod(unsigned int methodId){
  return methods[methodId];
}