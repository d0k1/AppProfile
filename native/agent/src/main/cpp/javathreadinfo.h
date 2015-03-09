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

#ifndef JAVATHREADINFO_H
#define JAVATHREADINFO_H

#include <string>
#include <sys/types.h>
#include <pthread.h>

using namespace std;

class JavaThreadInfo
{
public:
  JavaThreadInfo(string name, pid_t system_tid, pthread_t process_tid);
  JavaThreadInfo(pid_t system_tid, pthread_t process_tid);
  
  string getName();
  pid_t getSystemTid();
  pthread_t getProcessTid();
  
  bool isDead();
  void setDead();
private:
  bool dead;
  string name;
  pid_t system_tid;
  pthread_t process_tid;
};

#endif // JAVATHREADINFO_H
