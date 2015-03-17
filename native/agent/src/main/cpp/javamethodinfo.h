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

#ifndef JAVAMETHODINFO_H
#define JAVAMETHODINFO_H

#include <string>
#include "javaclassinfo.h"

using namespace std;

class JavaClassInfo;

class JavaMethodInfo
{
public:
  JavaMethodInfo(unsigned int classIndex, unsigned int methodIndex, const char *name, const char *signature, unsigned long long methodId, JavaClassInfo *info);
  JavaMethodInfo(unsigned int classIndex, unsigned int methodIndex, string name, string signature, unsigned long long methodId, JavaClassInfo *info);
  string getName(){return name;};
  string getSignature(){return signature;};
  unsigned long long getMethodId();
  JavaClassInfo *getClass();
  string getFQN();
private:
  unsigned int classIndex;
  unsigned int methodIndex;
  unsigned long long methodId;
  unsigned long long methodCounter;
  string name;
  string signature;  
  JavaClassInfo *classInfo;
};

#endif // JAVAMETHODINFO_H
