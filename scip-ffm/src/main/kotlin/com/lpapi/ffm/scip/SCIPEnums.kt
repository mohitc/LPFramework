package com.lpapi.ffm.scip

import org.scip.java.SCIP
import org.scip.java.SCIPDefs

enum class SCIPRetCode(val desc: String, val value: Int) {
    SCIP_UNKNOWN("unknown code", -1),
    SCIP_OKAY("normal termination", SCIP.SCIP_OKAY()),
    SCIP_ERROR("unspecified error", SCIP.SCIP_ERROR()),
    SCIP_NOMEMORY("insufficient memory error", SCIP.SCIP_NOMEMORY()),
    SCIP_READERROR("read error", SCIP.SCIP_READERROR()),
    SCIP_WRITEERROR("write error", SCIP.SCIP_WRITEERROR()),
    SCIP_NOFILE("file not found error", SCIP.SCIP_NOFILE()),
    SCIP_FILECREATEERROR("cannot create file", SCIP.SCIP_FILECREATEERROR()),
    SCIP_LPERROR("error in LP solver", SCIP.SCIP_LPERROR()),
    SCIP_NOPROBLEM("no problem exists", SCIP.SCIP_NOPROBLEM()),
    SCIP_INVALIDCALL("method cannot be called at this time in solution process", SCIP.SCIP_INVALIDCALL()),
    SCIP_INVALIDDATA("error in input data", SCIP.SCIP_INVALIDDATA()),
    SCIP_INVALIDRESULT("method returned an invalid result code", SCIP.SCIP_INVALIDRESULT()),
    SCIP_PLUGINNOTFOUND("a required plugin was not found", SCIP.SCIP_PLUGINNOTFOUND()),
    SCIP_PARAMETERUNKNOWN("the parameter with the given name was not found", SCIP.SCIP_PARAMETERUNKNOWN()),
    SCIP_PARAMETERWRONGTYPE("the parameter is not of the expected type", SCIP.SCIP_PARAMETERWRONGTYPE()),
    SCIP_PARAMETERWRONGVAL("the value is invalid for the given parameter", SCIP.SCIP_PARAMETERWRONGVAL()),
    SCIP_KEYALREADYEXISTING("the given key is already existing in table", SCIP.SCIP_KEYALREADYEXISTING()),
    SCIP_MAXDEPTHLEVEL("maximal branching depth level exceeded", SCIP.SCIP_MAXDEPTHLEVEL()),
    SCIP_BRANCHERROR("no branching could be created", SCIP.SCIP_BRANCHERROR()),
    SCIP_NOTIMPLEMENTED("function not implemented", SCIP.SCIP_NOTIMPLEMENTED());

    companion object {
        fun fromVal(intVal: Int): SCIPRetCode = SCIPRetCode.values().find { it.value == intVal } ?: SCIP_UNKNOWN
    }
}

enum class SCIPVarType(val desc: String, val value: Int) {
    SCIP_VARTYPE_BINARY("binary variable", SCIP.SCIP_VARTYPE_BINARY()),
    SCIP_VARTYPE_INTEGER("integer variable", SCIP.SCIP_VARTYPE_INTEGER()),
    SCIP_VARTYPE_IMPLINT(
        "implicit integer variable: Integrality of this variable is implied for every optimal solution of the remaining problem after any fixing all integer and binary variables, without the explicit need to enforce integrality further",
        SCIP.SCIP_VARTYPE_IMPLINT()
    ),
    SCIP_VARTYPE_CONTINUOUS("continuous variable", SCIP.SCIP_VARTYPE_CONTINUOUS())
}

enum class SCIPBool(val desc: String, val value: Int) {
    SCIP_TRUE("True", SCIPDefs.TRUE()),
    SCIP_FALSE("False", SCIPDefs.FALSE());
}

enum class SCIPStatus(val desc: String, val value: Int) {
    UNKNOWN("Unknown", SCIP.SCIP_STATUS_UNKNOWN()),
    USER_INTERRUPT("User Interrupted", SCIP.SCIP_STATUS_USERINTERRUPT()),
    NODE_LIMIT("Node Limit Reached", SCIP.SCIP_STATUS_NODELIMIT()),
    TOTAL_NODE_LIMIT("Total Node Limit Reached", SCIP.SCIP_STATUS_TOTALNODELIMIT()),
    STALL_NODE_LIMIT("Stalling Node Limit Reached", SCIP.SCIP_STATUS_STALLNODELIMIT()),
    TIME_LIMIT("Time Limit Reached", SCIP.SCIP_STATUS_TIMELIMIT()),
    MEMORY_LIMIT("Memory Limit Reached", SCIP.SCIP_STATUS_MEMLIMIT()),
    GAP_LIMIT("Gap Limit Reached", SCIP.SCIP_STATUS_GAPLIMIT()),
    PRIMAL_LIMIT("Primal Limit Reached", SCIP.SCIP_STATUS_PRIMALLIMIT()),
    DUAL_LIMIT("Dual Limit Reached", SCIP.SCIP_STATUS_DUALLIMIT()),
    SOLUTION_LIMIT("Solution Limit Reached", SCIP.SCIP_STATUS_SOLLIMIT()),
    BEST_SOLUTION_LIMIT("Solution Improvement Limit Reached", SCIP.SCIP_STATUS_BESTSOLLIMIT()),
    RESTART_LIMIT("Restart Limit Reached", SCIP.SCIP_STATUS_RESTARTLIMIT()),
    OPTIMAL("Optimal Solution", SCIP.SCIP_STATUS_OPTIMAL()),
    INFEASIBLE("Infesible", SCIP.SCIP_STATUS_INFEASIBLE()),
    UNBOUNDED("Unbounded", SCIP.SCIP_STATUS_UNBOUNDED()),
    INFEASIBLE_OR_UNBOUNDED("Infeasible or Unbounded", SCIP.SCIP_STATUS_INFORUNBD()),
    TERMINATED("Terminated", SCIP.SCIP_STATUS_TERMINATE());

    companion object {
        fun fromVal(intVal: Int): SCIPStatus = SCIPStatus.values().find { it.value == intVal } ?: UNKNOWN
    }

}