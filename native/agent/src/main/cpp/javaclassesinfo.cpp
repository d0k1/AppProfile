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

#include "javaclassesinfo.h"
#include "utils.h"

JavaMethodInfo *JavaClassesInfo::getMethodById(unsigned long id){
  return methods.at(id);
}

JavaClassesInfo::JavaClassesInfo():methodsCounter(0){
}

unsigned long JavaClassesInfo::getMethodsCount(){
  return methodsCounter;
}

unsigned int JavaClassesInfo::getClassesCount(){
  return classes.size();
}

unsigned int JavaClassesInfo::addClass(const char *name){
  classes.push_back(new JavaClassInfo(classes.size(), name));
  return classes.size()-1;
}

unsigned int JavaClassesInfo::addClass(string name){
  classes.push_back(new JavaClassInfo(classes.size(), name));
  return classes.size()-1;
}

string JavaClassesInfo::getClass(unsigned int id){
  return classes[id]->getName();
}

JavaMethodInfo *JavaClassesInfo::addClassMethod(unsigned int classId, unsigned int methodIndex, string methodName, string methodSignature){
  methodsCounter++;
  auto result = classes[classId]->addMethod(methodIndex, methodName, methodSignature, methodsCounter);
  methods.emplace(Utils::getMethodId(classId, methodIndex), result);
  return result;
}

JavaMethodInfo *JavaClassesInfo::addClassMethod(unsigned int classId, unsigned int methodIndex, const char *methodName, const char *methodSignature){
  methodsCounter++;
  auto result = classes[classId]->addMethod(methodIndex, methodName, methodSignature, methodsCounter);
  methods.emplace(Utils::getMethodId(classId, methodIndex), result);
  return result;
}

JavaMethodInfo *JavaClassesInfo::getMethodInfo(unsigned int classId, unsigned int methodId){
  return classes[classId]->getMethod(methodId);
}

