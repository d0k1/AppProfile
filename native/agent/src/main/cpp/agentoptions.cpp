/*
 * <one line to give the program's name and a brief idea of what it does.>
 * Copyright (C) 2015  Denis V. Kirpichenkov <email>
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

#include "agentoptions.h"
#include "utils.h"
#include <iostream>
#include <regex>
#include <boost/algorithm/string.hpp>

AgentOptions::AgentOptions(string filename){
  options_description desc("Options");
  variables_map vm;
  
  desc.add_options()("agent.exclude", boost::program_options::value<std::string>(&agentExclude));
  desc.add_options()("agent.exclude.ingore", boost::program_options::value<std::string>(&agentExcludeIgnore));
  desc.add_options()("agent.include", boost::program_options::value<std::string>(&agentInclude));
  desc.add_options()("agent.include.ingore", boost::program_options::value<std::string>(&agentIncludeIgnore));
  desc.add_options()("agent.appId", boost::program_options::value<std::string>(&appId));
  desc.add_options()("helper.jar", boost::program_options::value<std::string>(&helperJar));
  
  ifstream settings_file( filename , std::ifstream::in );
  store( parse_config_file( settings_file , desc, true ), vm );
  notify( vm );    
  settings_file.close();
  
  excludes = Utils::splitString(agentExclude, ",");
  excludesIgnore = Utils::splitString(agentExcludeIgnore, ",");
  includes = Utils::splitString(agentInclude, ",");
  includesIgnore = Utils::splitString(agentIncludeIgnore, ",");
}

bool matchMask(string value, string regexp){
  if(regexp.length()<3 || !boost::algorithm::starts_with(regexp, ".*")){
    return false;
  }
  regex e(regexp);
  
  return regex_match(value, e);
}

bool AgentOptions::isClassExcluded(const char *klass){
  string className(klass);
  boost::algorithm::replace_all<string>(className, "/", ".");
  bool skip = false;

  for(auto it=excludes.begin();it!=excludes.end();++it){
    if (boost::algorithm::starts_with(className, *it) || *it == "*" || matchMask(className, *it)){
      skip = true;
      break;
    }
  }

  for(auto it=excludesIgnore.begin();it!=excludesIgnore.end();++it){
    if(boost::algorithm::starts_with(className, *it)){
      skip = false;
      break;
    }
  }
  
  for(auto it=includes.begin();it!=includes.end();++it){
    if (boost::algorithm::starts_with(className, *it) || *it == "*" || matchMask(className, *it)){
      skip = false;
      break;
    } else {
      if(!skip){
	skip = true;
      }
    }
  }
  
  for(auto it=includesIgnore.begin();it!=includesIgnore.end();++it){
    if(boost::algorithm::starts_with(className, *it)){
      skip = false;
      break;
    }
  }

  return skip;
}

string AgentOptions::getHelperJar(){
  return helperJar;
}