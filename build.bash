#! /bin/bash

BASE_DIR=/usr/local/src
FLANN_INSTALL_DIR=/usr/local
USE_CUDA="" # Set to Y to compile the cuda support

cd $BASE_DIR
git clone https://github.com/mariusmuja/flann.git
cd flann
mkdir build
cd build

#The CUDA target architecture needs to be set appropriately
if [ "$USE_CUDA" == "Y" ]; then
  sed -e "s|sm_13|sm_30|" -i ../src/cpp/CMakeLists.txt
  cmake .. -DCMAKE_INSTALL_PREFIX=$FLANN_INSTALL -DCMAKE_BUILD_TYPE=Release -DBUILD_CUDA_LIB=ON
else
  cmake .. -DCMAKE_INSTALL_PREFIX=$FLANN_INSTALL -DCMAKE_BUILD_TYPE=Release
fi

#Actually make and install, assuming all went well
if [ "$?" == "0" ]; then
  make -j8
  sudo make install
fi

#