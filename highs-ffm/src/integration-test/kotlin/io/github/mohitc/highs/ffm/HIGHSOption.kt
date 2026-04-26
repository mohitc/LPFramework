package io.github.mohitc.highs.ffm

// Options to configure the Highs solver as defined at https://ergo-code.github.io/HiGHS/dev/options/definitions/

enum class HIGHSBoolOption(
  val option: String,
  val description: String,
  val default: Boolean,
) {
  OUTPUT_FLAG("output_flag", "Enables or disables solver output", true),
  LOG_TO_CONSOLE("log_to_console", "Enables or disables console logging", true),
  WRITE_MODEL_TO_FILE("write_model_to_file", "Write the model to a file", false),
  WRITE_PRESOLVED_MODEL_TO_FILE("write_presolved_model_to_file", "Write the presolved model to a file", false),
  WRITE_SOLUTION_TO_FILE("write_solution_to_file", "Write the primal and dual solution to a file", false),
  MIP_DETECT_SYMMETRY("mip_detect_symmetry", "Whether MIP symmetry should be detected", true),
  MIP_ALLOW_RESTART("mip_allow_restart", "Whether MIP restart is permitted", true),
  MIP_IMPROVING_SOLUTION_SAVE("mip_improving_solution_save", "Whether improving MIP solutions should be saved", false),
  MIP_IMPROVING_SOLUTION_REPORT_SPARSE(
    "mip_improving_solution_report_sparse",
    "Whether improving MIP solutions should be reported in sparse format",
    false,
  ),
  MIP_ROOT_PRESOLVE_ONLY("mip_root_presolve_only", "Whether MIP presolve is only applied at the root node", false),
  MIP_HEURISTIC_RUN_FEASIBILITY_JUMP(
    "mip_heuristic_run_feasibility_jump",
    "Use the feasibility jump heuristic",
    true,
  ),
  MIP_HEURISTIC_RUN_RINS("mip_heuristic_run_rins", "Use the RINS heuristic", true),
  MIP_HEURISTIC_RUN_RENS("mip_heuristic_run_rens", "Use the RENS heuristic", true),
  MIP_HEURISTIC_RUN_ROOT_REDUCED_COST(
    "mip_heuristic_run_root_reduced_cost",
    "Use the rootReducedCost heuristic",
    true,
  ),
  MIP_HEURISTIC_RUN_ZI_ROUND("mip_heuristic_run_zi_round", "Use the ZI Round heuristic", false),
  MIP_HEURISTIC_RUN_SHIFTING("mip_heuristic_run_shifting", "Use the Shifting heuristic", false),
  MIP_ALLOW_CUT_SEPARATION_AT_NODES(
    "mip_allow_cut_separation_at_nodes",
    "Whether cut separation at nodes other than the root node is permitted",
    true,
  ),
  BLEND_MULTI_OBJECTIVES("blend_multi_objectives", "Blend multiple objectives or apply lexicographically", true),
}

enum class HIGHSStringOption(
  val option: String,
  val description: String,
  val default: String,
) {
  PRESOLVE("presolve", "Presolve: \"off\", \"choose\" or \"on\"", "choose"),
  SOLVER(
    "solver",
    "LP/QP solver: \"choose\", \"simplex\", \"ipm\", \"ipx\", \"hipo\", \"pdlp\", \"qpasm\" or \"hipdlp\"",
    "choose",
  ),
  PARALLEL("parallel", "Parallel: \"off\", \"choose\" or \"on\"", "choose"),
  RUN_CROSSOVER("run_crossover", "Run IPM crossover: \"off\", \"choose\" or \"on\"", "on"),
  RANGING("ranging", "Compute cost, bound, RHS and basic solution ranging: \"off\" or \"on\"", "off"),
  LOG_FILE("log_file", "Log file", ""),
  READ_SOLUTION_FILE("read_solution_file", "Read solution file", ""),
  READ_BASIS_FILE("read_basis_file", "Read basis file", ""),
  WRITE_MODEL_FILE("write_model_file", "Write model file", ""),
  SOLUTION_FILE("solution_file", "Write solution file", ""),
  WRITE_BASIS_FILE("write_basis_file", "Write basis file", ""),
  WRITE_PRESOLVED_MODEL_FILE("write_presolved_model_file", "Write presolved model file", ""),
  WRITE_IIS_MODEL_FILE("write_iis_model_file", "Write IIS model file", ""),
  MIP_IMPROVING_SOLUTION_FILE(
    "mip_improving_solution_file",
    "File for reporting improving MIP solutions: not reported for an empty string \"\"",
    "",
  ),
  MIP_LP_SOLVER("mip_lp_solver", "MIP LP solver: \"choose\", \"simplex\", \"ipm\", \"ipx\" or \"hipo\"", "choose"),
  MIP_IPM_SOLVER("mip_ipm_solver", "MIP IPM solver: \"choose\", \"ipx\" or \"hipo\"", "choose"),
  HIPO_SYSTEM("hipo_system", "HiPO Newton system: \"choose\", \"augmented\" or \"normaleq\"", "choose"),
  HIPO_PARALLEL_TYPE("hipo_parallel_type", "HiPO parallelism: \"tree\", \"node\" or \"both\"", "both"),
  HIPO_ORDERING("hipo_ordering", "HiPO matrix reordering: \"choose\", \"metis\", \"amd\" or \"rcm\"", "choose"),
}

enum class HIGHSDoubleOption(
  val option: String,
  val description: String,
  val default: Double,
) {
  TIME_LIMIT("time_limit", "Time limit (seconds)", Double.POSITIVE_INFINITY),
  INFINITE_COST(
    "infinite_cost",
    "Limit on |cost coefficient|: values greater than or equal to this will be treated as infinite",
    1e+20,
  ),
  INFINITE_BOUND(
    "infinite_bound",
    "Limit on |constraint bound|: values greater than or equal to this will be treated as infinite",
    1e+20,
  ),
  SMALL_MATRIX_VALUE(
    "small_matrix_value",
    "Lower limit on |matrix entries|: values less than or equal to this will be treated as zero",
    1e-09,
  ),
  LARGE_MATRIX_VALUE(
    "large_matrix_value",
    "Upper limit on |matrix entries|: values greater than or equal to this will be treated as infinite",
    1e+15,
  ),
  KKT_TOLERANCE(
    "kkt_tolerance",
    "If changed from its default value, this tolerance is used for all feasibility and optimality (KKT) measures",
    1e-07,
  ),
  PRIMAL_FEASIBILITY_TOLERANCE("primal_feasibility_tolerance", "Primal feasibility tolerance", 1e-07),
  DUAL_FEASIBILITY_TOLERANCE("dual_feasibility_tolerance", "Dual feasibility tolerance", 1e-07),
  PRIMAL_RESIDUAL_TOLERANCE("primal_residual_tolerance", "Primal residual tolerance", 1e-07),
  DUAL_RESIDUAL_TOLERANCE("dual_residual_tolerance", "Dual residual tolerance", 1e-07),
  OPTIMALITY_TOLERANCE("optimality_tolerance", "Optimality tolerance", 1e-07),
  OBJECTIVE_BOUND(
    "objective_bound",
    "Objective bound for termination of the dual simplex solver",
    Double.POSITIVE_INFINITY,
  ),
  OBJECTIVE_TARGET("objective_target", "Objective target for termination of the MIP solver", Double.NEGATIVE_INFINITY),
  MIP_FEASIBILITY_TOLERANCE("mip_feasibility_tolerance", "MIP integrality tolerance", 1e-06),
  MIP_HEURISTIC_EFFORT("mip_heuristic_effort", "Effort spent for MIP heuristics", 0.05),
  MIP_REL_GAP(
    "mip_rel_gap",
    "Tolerance on relative gap, |ub-lb|/|ub|, to determine whether optimality has been reached for a MIP instance",
    0.0001,
  ),
  MIP_ABS_GAP(
    "mip_abs_gap",
    "Tolerance on absolute gap of MIP, |ub-lb|, to determine whether optimality has been reached for a MIP instance",
    1e-06,
  ),
  MIP_MIN_LOGGING_INTERVAL("mip_min_logging_interval", "MIP minimum logging interval", 5.0),
  IPM_OPTIMALITY_TOLERANCE("ipm_optimality_tolerance", "IPM optimality tolerance", 1e-08),
  PDLP_OPTIMALITY_TOLERANCE("pdlp_optimality_tolerance", "PDLP optimality tolerance", 1e-07),
  QP_REGULARIZATION_VALUE(
    "qp_regularization_value",
    "Regularization value added to the Hessian in the active set QP solver",
    1e-07,
  ),
  IIS_TIME_LIMIT("iis_time_limit", "Time limit for computing IIS (seconds)", Double.POSITIVE_INFINITY),
}

enum class HIGHSIntOption(
  val option: String,
  val description: String,
  val default: Int,
) {
  RANDOM_SEED("random_seed", "Random seed used in HiGHS", 0),
  THREADS("threads", "Number of threads used by HiGHS (0: automatic)", 0),
  USER_OBJECTIVE_SCALE("user_objective_scale", "Exponent of power-of-two objective scaling for model", 0),
  USER_BOUND_SCALE("user_bound_scale", "Exponent of power-of-two bound scaling for model", 0),
  SIMPLEX_STRATEGY(
    "simplex_strategy",
    "Strategy for simplex solver 0 => Choose; 1 => Dual (serial); 2 => Dual (SIP); 3 => Dual (PAMI); 4 => Primal",
    1,
  ),
  SIMPLEX_SCALE_STRATEGY(
    "simplex_scale_strategy",
    "Simplex scaling strategy: off / choose / equilibration (default) / forced equilibration / max value (0/1/2/3/4)",
    2,
  ),
  SIMPLEX_DUAL_EDGE_WEIGHT_STRATEGY(
    "simplex_dual_edge_weight_strategy",
    "Strategy for simplex dual edge weights: Choose / Dantzig / Devex / Steepest Edge (-1/0/1/2)",
    -1,
  ),
  SIMPLEX_PRIMAL_EDGE_WEIGHT_STRATEGY(
    "simplex_primal_edge_weight_strategy",
    "Strategy for simplex primal edge weights: Choose / Dantzig / Devex / Steepest Edge (-1/0/1/2)",
    -1,
  ),
  SIMPLEX_ITERATION_LIMIT(
    "simplex_iteration_limit",
    "Iteration limit for simplex solver when solving LPs, but not subproblems in the MIP solver",
    2147483647,
  ),
  SIMPLEX_UPDATE_LIMIT("simplex_update_limit", "Limit on the number of simplex UPDATE operations", 5000),
  SIMPLEX_MAX_CONCURRENCY("simplex_max_concurrency", "Maximum level of concurrency in parallel simplex", 8),
  WRITE_SOLUTION_STYLE(
    "write_solution_style",
    "Style of solution file (raw = computer-readable, pretty = human-readable): -1 => HiGHS old raw (deprecated); 0 => HiGHS raw; 1 => HiGHS pretty; 2 => Glpsol raw; 3 => Glpsol pretty; 4 => HiGHS sparse raw",
    0,
  ),
  GLPSOL_COST_ROW_LOCATION(
    "glpsol_cost_row_location",
    "Location of cost row for Glpsol file: -2 => Last; -1 => None; 0 => None if empty, otherwise data file location; 1 <= n <= num_row => Location n; n > num_row => Last",
    0,
  ),
  MIP_MAX_NODES("mip_max_nodes", "MIP solver max number of nodes", 2147483647),
  MIP_MAX_STALL_NODES(
    "mip_max_stall_nodes",
    "MIP solver max number of nodes where estimate is above cutoff bound",
    2147483647,
  ),
  MIP_MAX_START_NODES(
    "mip_max_start_nodes",
    "MIP solver max number of nodes when completing a partial MIP start",
    500,
  ),
  MIP_LIFTING_FOR_PROBING("mip_lifting_for_probing", "Level of lifting for probing that is used", -1),
  MIP_MAX_LEAVES("mip_max_leaves", "MIP solver max number of leaf nodes", 2147483647),
  MIP_MAX_IMPROVING_SOLS(
    "mip_max_improving_sols",
    "Limit on the number of improving solutions found to stop the MIP solver prematurely",
    2147483647,
  ),
  MIP_LP_AGE_LIMIT(
    "mip_lp_age_limit",
    "Maximal age of dynamic LP rows before they are removed from the LP relaxation in the MIP solver",
    10,
  ),
  MIP_POOL_AGE_LIMIT(
    "mip_pool_age_limit",
    "Maximal age of rows in the MIP solver cutpool before they are deleted",
    30,
  ),
  MIP_POOL_SOFT_LIMIT(
    "mip_pool_soft_limit",
    "Soft limit on the number of rows in the MIP solver cutpool for dynamic age adjustment",
    10000,
  ),
  MIP_PSCOST_MINRELIABLE(
    "mip_pscost_minreliable",
    "Minimal number of observations before MIP solver pseudo costs are considered reliable",
    8,
  ),
  MIP_MIN_CLIQUETABLE_ENTRIES_FOR_PARALLELISM(
    "mip_min_cliquetable_entries_for_parallelism",
    "Minimal number of entries in the MIP solver cliquetable before neighbourhood queries of the conflict graph use parallel processing",
    100000,
  ),
  IPM_ITERATION_LIMIT("ipm_iteration_limit", "Iteration limit for IPM solver", 2147483647),
  HIPO_BLOCK_SIZE("hipo_block_size", "Block size for dense linear algebra within HiPO", 128),
  PDLP_ITERATION_LIMIT("pdlp_iteration_limit", "Iteration limit for PDLP solver", 2147483647),
  PDLP_SCALING_MODE(
    "pdlp_scaling_mode",
    "Scaling mode for PDLP solver (default = 5): 1 => Ruiz; 2 => L2; 4 => PC",
    5,
  ),
  PDLP_RUIZ_ITERATIONS("pdlp_ruiz_iterations", "Number of Ruiz scaling iteraitons for PDLP solver", 10),
  PDLP_RESTART_STRATEGY(
    "pdlp_restart_strategy",
    "Restart strategy for PDLP solver: 0 => off; 1 => fixed; 2 => adaptive; 3 => Halpern",
    2,
  ),
  PDLP_CUPDLPC_RESTART_METHOD(
    "pdlp_cupdlpc_restart_method",
    "Restart mode for cuPDLP-C solver: 0 => none; 1 => GPU (default); 2 => CPU",
    1,
  ),
  PDLP_STEP_SIZE_STRATEGY(
    "pdlp_step_size_strategy",
    "Step size strategy for PDLP solver: 0 => fixed; 1 => adaptive; 2 => Malitsky-Pock; 3 => PID",
    1,
  ),
  QP_ITERATION_LIMIT("qp_iteration_limit", "Iteration limit for the active set QP solver", 2147483647),
  QP_NULLSPACE_LIMIT("qp_nullspace_limit", "Nullspace limit for the active set QP solver", 4000),
  IIS_STRATEGY(
    "iis_strategy",
    "Strategy for IIS calculation: 0 => Light test; 1 => Try dual ray; 2 => Try elastic LP; 4 => Prioritise columns; 8 => Find true IIS; 16 => Find relaxation IIS for MIP",
    0,
  ),
}