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
#include <iostream>

AgentOptions::AgentOptions(string properties){
  options_description desc("Options");
  
  desc.add_options()("agent.exclude", boost::program_options::value<std::string>(&agentExclude));
  desc.add_options()("agent.exclude.ingore", boost::program_options::value<std::string>(&agentExcludeIgnore));
  desc.add_options()("agent.include", boost::program_options::value<std::string>(&agentInclude));
  desc.add_options()("agent.include.ingore", boost::program_options::value<std::string>(&agentIncludeIgnore));
  desc.add_options()("agent.appId", boost::program_options::value<std::string>(&appId));
  
  ifstream settings_file( properties , std::ifstream::in );
  store( parse_config_file( settings_file , desc ), vm );
  notify( vm );    
  settings_file.close();
}
