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

#ifndef JAVATHREADSINFO_H
#define JAVATHREADSINFO_H

#include <vector>
#include <map>
#include "javathreadinfo.h"

using namespace std;

class JavaThreadsInfo
{
public:
    JavaThreadsInfo();
    void addThread (JavaThreadInfo info);
    void setThreadDead(JavaThreadInfo info);
    unsigned int getThreadCount();
    JavaThreadInfo getThread(unsigned int id);
private:
    vector<JavaThreadInfo> threads;
    map<pthread_t, unsigned int> threadMap;
};

#endif // JAVATHREADSINFO_H
