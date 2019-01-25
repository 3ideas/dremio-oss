/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dremio.exec.planner.physical;

import java.io.IOException;
import java.util.List;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;

import com.dremio.exec.physical.base.PhysicalOperator;
import com.dremio.exec.physical.config.UnorderedDeMuxExchange;
import com.dremio.exec.planner.physical.DistributionTrait.DistributionField;
import com.dremio.exec.record.BatchSchema.SelectionVectorMode;

public class UnorderedDeMuxExchangePrel extends ExchangePrel {

  private final List<DistributionField> fields;

  public UnorderedDeMuxExchangePrel(RelOptCluster cluster, RelTraitSet traits, RelNode child, List<DistributionField> fields) {
    super(cluster, traits, child);
    this.fields = fields;
  }

  @Override
  public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
    return new UnorderedDeMuxExchangePrel(getCluster(), traitSet, sole(inputs), fields);
  }

  @Override
  public PhysicalOperator getPhysicalOperator(PhysicalPlanCreator creator) throws IOException {
    Prel child = (Prel) this.getInput();

    PhysicalOperator childPOP = child.getPhysicalOperator(creator);

    UnorderedDeMuxExchange p = new UnorderedDeMuxExchange(childPOP, HashPrelUtil.getHashExpression(this.fields, getInput().getRowType()));
    return creator.addMetadata(this, p);
  }

  @Override
  public SelectionVectorMode getEncoding() {
    return SelectionVectorMode.NONE;
  }

  @Override
  public SelectionVectorMode[] getSupportedEncodings() {
    // DeMuxExchangePrel accepts vectors with all types SelectionVectors as input.
    return SelectionVectorMode.ALL;
  }
}
