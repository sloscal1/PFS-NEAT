%module java_flann
%{
#include "/usr/local/src/flann/src/cpp/flann/defines.h"
#include "/usr/local/src/flann/src/cpp/flann/flann.h"
%}
%include "/usr/local/src/flann/src/cpp/flann/defines.h"
%include "/usr/local/src/flann/src/cpp/flann/flann.h"
%include cpointer.i
%pointer_functions(double, doublep);
%pointer_functions(float, floatp);
