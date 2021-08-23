#!/usr/bin/env bash
# BEGIN config
PLUGIN_NAME="chatplugin"
# END config

sourceBase=$(dirname $SOURCE)/../
cd "${basedir:-$sourceBase}"

basedir=$(pwd -P)
cd -


function bashColor {
  if [ $2 ]; then
      echo -e "\e[$1;$2m"
  else
      echo -e "\e[$1m"
  fi
}

function bashColorReset {
    echo -e "\e[m"
}

function basedir {
    cd "$basedir"
}

function gethead {
    git log -1 --oneline
}
