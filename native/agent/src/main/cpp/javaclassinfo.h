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

#ifndef JAVACLASSINFO_H
#define JAVACLASSINFO_H

#include <string>
#include <vector>
#include "javamethodinfo.h"

using namespace std;

class JavaMethodInfo;

class JavaClassInfo
{
public:
  JavaClassInfo(unsigned int classIndex, string name);  
  JavaClassInfo(unsigned int classIndex, const char *name);  
  
  JavaMethodInfo *addMethod(unsigned int methodIndex, string name, string signature, unsigned long long methodId);
  JavaMethodInfo *addMethod(unsigned int methodIndex, const char *name, const char *signature, unsigned long long methodId);
  string getName(){return name;};
  
  unsigned int getMethodCount();
  
  JavaMethodInfo *getMethod(unsigned int methodId);
private:
  unsigned int classIndex;
  string name;
  vector<JavaMethodInfo*> methods;
};

#endif // JAVACLASSINFO_H
