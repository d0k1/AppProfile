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

#include "utils.h"

unsigned long long Utils::getMethodId(int cnum, int mnum){
  unsigned long long result = cnum;
  return result<<32|mnum;
}

vector<string> Utils::splitString(string value, const char *separator){
  vector<string> res;
  typedef tokenizer<char_separator<char> > tokenizer;
  char_separator<char> sepa(separator);
  tokenizer tokens(value, sepa);
  for (const auto& t : tokens) {
    res.push_back(string(t));
  }

  return res;
}
