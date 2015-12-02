/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package flann;

public class java_flann {
  public static void setDEFAULT_FLANN_PARAMETERS(FLANNParameters value) {
    java_flannJNI.DEFAULT_FLANN_PARAMETERS_set(FLANNParameters.getCPtr(value), value);
  }

  public static FLANNParameters getDEFAULT_FLANN_PARAMETERS() {
    long cPtr = java_flannJNI.DEFAULT_FLANN_PARAMETERS_get();
    return (cPtr == 0) ? null : new FLANNParameters(cPtr, false);
  }

  public static void flann_log_verbosity(int level) {
    java_flannJNI.flann_log_verbosity(level);
  }

  public static void flann_set_distance_type(flann_distance_t distance_type, int order) {
    java_flannJNI.flann_set_distance_type(distance_type.swigValue(), order);
  }

  public static flann_distance_t flann_get_distance_type() {
    return flann_distance_t.swigToEnum(java_flannJNI.flann_get_distance_type());
  }

  public static int flann_get_distance_order() {
    return java_flannJNI.flann_get_distance_order();
  }

  public static SWIGTYPE_p_void flann_build_index(SWIGTYPE_p_float dataset, int rows, int cols, SWIGTYPE_p_float speedup, FLANNParameters flann_params) {
    long cPtr = java_flannJNI.flann_build_index(SWIGTYPE_p_float.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(speedup), FLANNParameters.getCPtr(flann_params), flann_params);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_build_index_float(SWIGTYPE_p_float dataset, int rows, int cols, SWIGTYPE_p_float speedup, FLANNParameters flann_params) {
    long cPtr = java_flannJNI.flann_build_index_float(SWIGTYPE_p_float.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(speedup), FLANNParameters.getCPtr(flann_params), flann_params);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_build_index_double(SWIGTYPE_p_double dataset, int rows, int cols, SWIGTYPE_p_float speedup, FLANNParameters flann_params) {
    long cPtr = java_flannJNI.flann_build_index_double(SWIGTYPE_p_double.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(speedup), FLANNParameters.getCPtr(flann_params), flann_params);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_build_index_byte(SWIGTYPE_p_unsigned_char dataset, int rows, int cols, SWIGTYPE_p_float speedup, FLANNParameters flann_params) {
    long cPtr = java_flannJNI.flann_build_index_byte(SWIGTYPE_p_unsigned_char.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(speedup), FLANNParameters.getCPtr(flann_params), flann_params);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_build_index_int(SWIGTYPE_p_int dataset, int rows, int cols, SWIGTYPE_p_float speedup, FLANNParameters flann_params) {
    long cPtr = java_flannJNI.flann_build_index_int(SWIGTYPE_p_int.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(speedup), FLANNParameters.getCPtr(flann_params), flann_params);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static int flann_add_points(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_float points, int rows, int columns, float rebuild_threshold) {
    return java_flannJNI.flann_add_points(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_float.getCPtr(points), rows, columns, rebuild_threshold);
  }

  public static int flann_add_points_float(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_float points, int rows, int columns, float rebuild_threshold) {
    return java_flannJNI.flann_add_points_float(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_float.getCPtr(points), rows, columns, rebuild_threshold);
  }

  public static int flann_add_points_double(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_double points, int rows, int columns, float rebuild_threshold) {
    return java_flannJNI.flann_add_points_double(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_double.getCPtr(points), rows, columns, rebuild_threshold);
  }

  public static int flann_add_points_byte(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_unsigned_char points, int rows, int columns, float rebuild_threshold) {
    return java_flannJNI.flann_add_points_byte(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_unsigned_char.getCPtr(points), rows, columns, rebuild_threshold);
  }

  public static int flann_add_points_int(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_int points, int rows, int columns, float rebuild_threshold) {
    return java_flannJNI.flann_add_points_int(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_int.getCPtr(points), rows, columns, rebuild_threshold);
  }

  public static int flann_remove_point(SWIGTYPE_p_void index_ptr, long point_id) {
    return java_flannJNI.flann_remove_point(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
  }

  public static int flann_remove_point_float(SWIGTYPE_p_void index_ptr, long point_id) {
    return java_flannJNI.flann_remove_point_float(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
  }

  public static int flann_remove_point_double(SWIGTYPE_p_void index_ptr, long point_id) {
    return java_flannJNI.flann_remove_point_double(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
  }

  public static int flann_remove_point_byte(SWIGTYPE_p_void index_ptr, long point_id) {
    return java_flannJNI.flann_remove_point_byte(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
  }

  public static int flann_remove_point_int(SWIGTYPE_p_void index_ptr, long point_id) {
    return java_flannJNI.flann_remove_point_int(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
  }

  public static SWIGTYPE_p_float flann_get_point(SWIGTYPE_p_void index_ptr, long point_id) {
    long cPtr = java_flannJNI.flann_get_point(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }

  public static SWIGTYPE_p_float flann_get_point_float(SWIGTYPE_p_void index_ptr, long point_id) {
    long cPtr = java_flannJNI.flann_get_point_float(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }

  public static SWIGTYPE_p_double flann_get_point_double(SWIGTYPE_p_void index_ptr, long point_id) {
    long cPtr = java_flannJNI.flann_get_point_double(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
    return (cPtr == 0) ? null : new SWIGTYPE_p_double(cPtr, false);
  }

  public static SWIGTYPE_p_unsigned_char flann_get_point_byte(SWIGTYPE_p_void index_ptr, long point_id) {
    long cPtr = java_flannJNI.flann_get_point_byte(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_char(cPtr, false);
  }

  public static SWIGTYPE_p_int flann_get_point_int(SWIGTYPE_p_void index_ptr, long point_id) {
    long cPtr = java_flannJNI.flann_get_point_int(SWIGTYPE_p_void.getCPtr(index_ptr), point_id);
    return (cPtr == 0) ? null : new SWIGTYPE_p_int(cPtr, false);
  }

  public static long flann_veclen(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_veclen(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_veclen_float(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_veclen_float(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_veclen_double(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_veclen_double(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_veclen_byte(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_veclen_byte(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_veclen_int(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_veclen_int(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_size(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_size(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_size_float(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_size_float(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_size_double(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_size_double(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_size_byte(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_size_byte(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static long flann_size_int(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_size_int(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static int flann_used_memory(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_used_memory(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static int flann_used_memory_float(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_used_memory_float(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static int flann_used_memory_double(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_used_memory_double(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static int flann_used_memory_byte(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_used_memory_byte(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static int flann_used_memory_int(SWIGTYPE_p_void index_ptr) {
    return java_flannJNI.flann_used_memory_int(SWIGTYPE_p_void.getCPtr(index_ptr));
  }

  public static int flann_save_index(SWIGTYPE_p_void index_id, String filename) {
    return java_flannJNI.flann_save_index(SWIGTYPE_p_void.getCPtr(index_id), filename);
  }

  public static int flann_save_index_float(SWIGTYPE_p_void index_id, String filename) {
    return java_flannJNI.flann_save_index_float(SWIGTYPE_p_void.getCPtr(index_id), filename);
  }

  public static int flann_save_index_double(SWIGTYPE_p_void index_id, String filename) {
    return java_flannJNI.flann_save_index_double(SWIGTYPE_p_void.getCPtr(index_id), filename);
  }

  public static int flann_save_index_byte(SWIGTYPE_p_void index_id, String filename) {
    return java_flannJNI.flann_save_index_byte(SWIGTYPE_p_void.getCPtr(index_id), filename);
  }

  public static int flann_save_index_int(SWIGTYPE_p_void index_id, String filename) {
    return java_flannJNI.flann_save_index_int(SWIGTYPE_p_void.getCPtr(index_id), filename);
  }

  public static SWIGTYPE_p_void flann_load_index(String filename, SWIGTYPE_p_float dataset, int rows, int cols) {
    long cPtr = java_flannJNI.flann_load_index(filename, SWIGTYPE_p_float.getCPtr(dataset), rows, cols);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_load_index_float(String filename, SWIGTYPE_p_float dataset, int rows, int cols) {
    long cPtr = java_flannJNI.flann_load_index_float(filename, SWIGTYPE_p_float.getCPtr(dataset), rows, cols);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_load_index_double(String filename, SWIGTYPE_p_double dataset, int rows, int cols) {
    long cPtr = java_flannJNI.flann_load_index_double(filename, SWIGTYPE_p_double.getCPtr(dataset), rows, cols);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_load_index_byte(String filename, SWIGTYPE_p_unsigned_char dataset, int rows, int cols) {
    long cPtr = java_flannJNI.flann_load_index_byte(filename, SWIGTYPE_p_unsigned_char.getCPtr(dataset), rows, cols);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void flann_load_index_int(String filename, SWIGTYPE_p_int dataset, int rows, int cols) {
    long cPtr = java_flannJNI.flann_load_index_int(filename, SWIGTYPE_p_int.getCPtr(dataset), rows, cols);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static int flann_find_nearest_neighbors(SWIGTYPE_p_float dataset, int rows, int cols, SWIGTYPE_p_float testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors(SWIGTYPE_p_float.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_float(SWIGTYPE_p_float dataset, int rows, int cols, SWIGTYPE_p_float testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_float(SWIGTYPE_p_float.getCPtr(dataset), rows, cols, SWIGTYPE_p_float.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_double(SWIGTYPE_p_double dataset, int rows, int cols, SWIGTYPE_p_double testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_double dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_double(SWIGTYPE_p_double.getCPtr(dataset), rows, cols, SWIGTYPE_p_double.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_double.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_byte(SWIGTYPE_p_unsigned_char dataset, int rows, int cols, SWIGTYPE_p_unsigned_char testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_byte(SWIGTYPE_p_unsigned_char.getCPtr(dataset), rows, cols, SWIGTYPE_p_unsigned_char.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_int(SWIGTYPE_p_int dataset, int rows, int cols, SWIGTYPE_p_int testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_int(SWIGTYPE_p_int.getCPtr(dataset), rows, cols, SWIGTYPE_p_int.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_index(SWIGTYPE_p_void index_id, SWIGTYPE_p_float testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_index(SWIGTYPE_p_void.getCPtr(index_id), SWIGTYPE_p_float.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_index_float(SWIGTYPE_p_void index_id, SWIGTYPE_p_float testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_index_float(SWIGTYPE_p_void.getCPtr(index_id), SWIGTYPE_p_float.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_index_double(SWIGTYPE_p_void index_id, SWIGTYPE_p_double testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_double dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_index_double(SWIGTYPE_p_void.getCPtr(index_id), SWIGTYPE_p_double.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_double.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_index_byte(SWIGTYPE_p_void index_id, SWIGTYPE_p_unsigned_char testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_index_byte(SWIGTYPE_p_void.getCPtr(index_id), SWIGTYPE_p_unsigned_char.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_find_nearest_neighbors_index_int(SWIGTYPE_p_void index_id, SWIGTYPE_p_int testset, int trows, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int nn, FLANNParameters flann_params) {
    return java_flannJNI.flann_find_nearest_neighbors_index_int(SWIGTYPE_p_void.getCPtr(index_id), SWIGTYPE_p_int.getCPtr(testset), trows, SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), nn, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_radius_search(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_float query, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int max_nn, float radius, FLANNParameters flann_params) {
    return java_flannJNI.flann_radius_search(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_float.getCPtr(query), SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), max_nn, radius, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_radius_search_float(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_float query, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int max_nn, float radius, FLANNParameters flann_params) {
    return java_flannJNI.flann_radius_search_float(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_float.getCPtr(query), SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), max_nn, radius, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_radius_search_double(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_double query, SWIGTYPE_p_int indices, SWIGTYPE_p_double dists, int max_nn, float radius, FLANNParameters flann_params) {
    return java_flannJNI.flann_radius_search_double(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_double.getCPtr(query), SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_double.getCPtr(dists), max_nn, radius, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_radius_search_byte(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_unsigned_char query, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int max_nn, float radius, FLANNParameters flann_params) {
    return java_flannJNI.flann_radius_search_byte(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_unsigned_char.getCPtr(query), SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), max_nn, radius, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_radius_search_int(SWIGTYPE_p_void index_ptr, SWIGTYPE_p_int query, SWIGTYPE_p_int indices, SWIGTYPE_p_float dists, int max_nn, float radius, FLANNParameters flann_params) {
    return java_flannJNI.flann_radius_search_int(SWIGTYPE_p_void.getCPtr(index_ptr), SWIGTYPE_p_int.getCPtr(query), SWIGTYPE_p_int.getCPtr(indices), SWIGTYPE_p_float.getCPtr(dists), max_nn, radius, FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_free_index(SWIGTYPE_p_void index_id, FLANNParameters flann_params) {
    return java_flannJNI.flann_free_index(SWIGTYPE_p_void.getCPtr(index_id), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_free_index_float(SWIGTYPE_p_void index_id, FLANNParameters flann_params) {
    return java_flannJNI.flann_free_index_float(SWIGTYPE_p_void.getCPtr(index_id), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_free_index_double(SWIGTYPE_p_void index_id, FLANNParameters flann_params) {
    return java_flannJNI.flann_free_index_double(SWIGTYPE_p_void.getCPtr(index_id), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_free_index_byte(SWIGTYPE_p_void index_id, FLANNParameters flann_params) {
    return java_flannJNI.flann_free_index_byte(SWIGTYPE_p_void.getCPtr(index_id), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_free_index_int(SWIGTYPE_p_void index_id, FLANNParameters flann_params) {
    return java_flannJNI.flann_free_index_int(SWIGTYPE_p_void.getCPtr(index_id), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_compute_cluster_centers(SWIGTYPE_p_float dataset, int rows, int cols, int clusters, SWIGTYPE_p_float result, FLANNParameters flann_params) {
    return java_flannJNI.flann_compute_cluster_centers(SWIGTYPE_p_float.getCPtr(dataset), rows, cols, clusters, SWIGTYPE_p_float.getCPtr(result), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_compute_cluster_centers_float(SWIGTYPE_p_float dataset, int rows, int cols, int clusters, SWIGTYPE_p_float result, FLANNParameters flann_params) {
    return java_flannJNI.flann_compute_cluster_centers_float(SWIGTYPE_p_float.getCPtr(dataset), rows, cols, clusters, SWIGTYPE_p_float.getCPtr(result), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_compute_cluster_centers_double(SWIGTYPE_p_double dataset, int rows, int cols, int clusters, SWIGTYPE_p_double result, FLANNParameters flann_params) {
    return java_flannJNI.flann_compute_cluster_centers_double(SWIGTYPE_p_double.getCPtr(dataset), rows, cols, clusters, SWIGTYPE_p_double.getCPtr(result), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_compute_cluster_centers_byte(SWIGTYPE_p_unsigned_char dataset, int rows, int cols, int clusters, SWIGTYPE_p_float result, FLANNParameters flann_params) {
    return java_flannJNI.flann_compute_cluster_centers_byte(SWIGTYPE_p_unsigned_char.getCPtr(dataset), rows, cols, clusters, SWIGTYPE_p_float.getCPtr(result), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static int flann_compute_cluster_centers_int(SWIGTYPE_p_int dataset, int rows, int cols, int clusters, SWIGTYPE_p_float result, FLANNParameters flann_params) {
    return java_flannJNI.flann_compute_cluster_centers_int(SWIGTYPE_p_int.getCPtr(dataset), rows, cols, clusters, SWIGTYPE_p_float.getCPtr(result), FLANNParameters.getCPtr(flann_params), flann_params);
  }

  public static SWIGTYPE_p_double new_doublep() {
    long cPtr = java_flannJNI.new_doublep();
    return (cPtr == 0) ? null : new SWIGTYPE_p_double(cPtr, false);
  }

  public static SWIGTYPE_p_double copy_doublep(double value) {
    long cPtr = java_flannJNI.copy_doublep(value);
    return (cPtr == 0) ? null : new SWIGTYPE_p_double(cPtr, false);
  }

  public static void delete_doublep(SWIGTYPE_p_double obj) {
    java_flannJNI.delete_doublep(SWIGTYPE_p_double.getCPtr(obj));
  }

  public static void doublep_assign(SWIGTYPE_p_double obj, double value) {
    java_flannJNI.doublep_assign(SWIGTYPE_p_double.getCPtr(obj), value);
  }

  public static double doublep_value(SWIGTYPE_p_double obj) {
    return java_flannJNI.doublep_value(SWIGTYPE_p_double.getCPtr(obj));
  }

  public static SWIGTYPE_p_float new_floatp() {
    long cPtr = java_flannJNI.new_floatp();
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }

  public static SWIGTYPE_p_float copy_floatp(float value) {
    long cPtr = java_flannJNI.copy_floatp(value);
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }

  public static void delete_floatp(SWIGTYPE_p_float obj) {
    java_flannJNI.delete_floatp(SWIGTYPE_p_float.getCPtr(obj));
  }

  public static void floatp_assign(SWIGTYPE_p_float obj, float value) {
    java_flannJNI.floatp_assign(SWIGTYPE_p_float.getCPtr(obj), value);
  }

  public static float floatp_value(SWIGTYPE_p_float obj) {
    return java_flannJNI.floatp_value(SWIGTYPE_p_float.getCPtr(obj));
  }

}