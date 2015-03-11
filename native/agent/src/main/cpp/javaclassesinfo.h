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

#ifndef JAVACLASSESINFO_H
#define JAVACLASSESINFO_H

#include "javaclassinfo.h"
#include "javamethodinfo.h"

#include <vector>
#include <map>
using namespace std;

class JavaClassesInfo
{
public:
  JavaClassesInfo();
  
  unsigned int addClass(const char *name);
  unsigned int addClass(string name);
  
  string getClass(unsigned int id);
  
  unsigned int getClassesCount();
  unsigned long getMethodsCount();
  
  JavaMethodInfo *addClassMethod(unsigned int classId, string methodName, string methodSignature);
  JavaMethodInfo *addClassMethod(unsigned int classId, const char *methodName, const char *methodSignature);
  
  JavaMethodInfo *getMethodInfo(unsigned int classId, unsigned int methodId);
  
  JavaMethodInfo *getMethodById(unsigned long id);
private:
  unsigned long methodsCounter;
  map<unsigned long, JavaMethodInfo*> methods;
  vector<JavaClassInfo*> classes;
};

#endif // JAVACLASSESINFO_H
