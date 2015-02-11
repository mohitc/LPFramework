package com.lpapi.export.jsonfile.exportdto;

import com.lpapi.entities.LPExpression;
import com.lpapi.entities.LPExpressionTerm;

import java.util.ArrayList;
import java.util.List;

public class LPExpressionDTO {
  private List<LPExpressionTermDTO> termList;

  public LPExpressionDTO(){}

  public LPExpressionDTO(LPExpression expression) {
    termList = new ArrayList<>();
    for (LPExpressionTerm term: expression.getTermList()) {
      termList.add(new LPExpressionTermDTO(term));
    }
  }

  public List<LPExpressionTermDTO> getTermList() {
    return termList;
  }

  public void setTermList(List<LPExpressionTermDTO> termList) {
    this.termList = termList;
  }
}
