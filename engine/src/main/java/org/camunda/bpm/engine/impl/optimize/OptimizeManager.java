/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.optimize;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricDecisionInstanceQueryImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.optimize.OptimizeHistoricIdentityLinkLogEntity;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

public class OptimizeManager extends AbstractManager {

  /**
   * Loads the byte arrays into the cache; does currently not return a list because it is not needed
   * by the calling code and we can avoid concatenating lists in the implementation that way.
   */
  public void fetchHistoricVariableUpdateByteArrays(List<String> byteArrayIds) {

    List<List<String>> partitions = CollectionUtil.partition(byteArrayIds,
        DbSqlSessionFactory.MAXIMUM_NUMBER_PARAMS);

    for (List<String> partition : partitions) {
      getDbEntityManager().selectList("selectByteArrays", partition);
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> getCompletedHistoricActivityInstances(Date finishedAfter,
      Date finishedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("finishedAfter", finishedAfter);
    params.put("finishedAt", finishedAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectCompletedHistoricActivityPage", params);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> getRunningHistoricActivityInstances(Date startedAfter,
      Date startedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("startedAfter", startedAfter);
    params.put("startedAt", startedAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectRunningHistoricActivityPage", params);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> getCompletedHistoricTaskInstances(Date finishedAfter,
      Date finishedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("finishedAfter", finishedAfter);
    params.put("finishedAt", finishedAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectCompletedHistoricTaskInstancePage", params);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> getRunningHistoricTaskInstances(Date startedAfter,
      Date startedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("startedAfter", startedAfter);
    params.put("startedAt", startedAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectRunningHistoricTaskInstancePage", params);
  }

  @SuppressWarnings("unchecked")
  public List<UserOperationLogEntry> getHistoricUserOperationLogs(Date occurredAfter,
      Date occurredAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    String[] operationTypes = new String[] { UserOperationLogEntry.OPERATION_TYPE_ASSIGN,
        UserOperationLogEntry.OPERATION_TYPE_CLAIM, UserOperationLogEntry.OPERATION_TYPE_COMPLETE };
    Map<String, Object> params = new HashMap<>();
    params.put("occurredAfter", occurredAfter);
    params.put("occurredAt", occurredAt);
    params.put("operationTypes", operationTypes);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectHistoricUserOperationLogPage", params);
  }

  @SuppressWarnings("unchecked")
  public List<OptimizeHistoricIdentityLinkLogEntity> getHistoricIdentityLinkLogs(Date occurredAfter,
      Date occurredAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("occurredAfter", occurredAfter);
    params.put("occurredAt", occurredAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectHistoricIdentityLinkPage", params);
  }

  private void checkIsAuthorizedToReadHistoryOfProcessDefinitions() {
    getAuthorizationManager().checkAuthorization(READ_HISTORY, PROCESS_DEFINITION);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> getCompletedHistoricProcessInstances(Date finishedAfter,
      Date finishedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("finishedAfter", finishedAfter);
    params.put("finishedAt", finishedAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectCompletedHistoricProcessInstancePage", params);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> getRunningHistoricProcessInstances(Date startedAfter,
      Date startedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("startedAfter", startedAfter);
    params.put("startedAt", startedAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectRunningHistoricProcessInstancePage", params);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableUpdate> getHistoricVariableUpdates(Date occurredAfter,
      Date occurredAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfProcessDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("occurredAfter", occurredAfter);
    params.put("occurredAt", occurredAt);
    params.put("maxResults", maxResults);

    return getDbEntityManager().selectList("selectHistoricVariableUpdatePage", params);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDecisionInstance> getHistoricDecisionInstances(Date evaluatedAfter,
      Date evaluatedAt, int maxResults) {
    checkIsAuthorizedToReadHistoryOfDecisionDefinitions();

    Map<String, Object> params = new HashMap<>();
    params.put("evaluatedAfter", evaluatedAfter);
    params.put("evaluatedAt", evaluatedAt);
    params.put("maxResults", maxResults);

    List<HistoricDecisionInstance> decisionInstances = getDbEntityManager()
        .selectList("selectHistoricDecisionInstancePage", params);

    HistoricDecisionInstanceQueryImpl query = (HistoricDecisionInstanceQueryImpl) new HistoricDecisionInstanceQueryImpl()
        .disableBinaryFetching().disableCustomObjectDeserialization().includeInputs()
        .includeOutputs();

    getHistoricDecisionInstanceManager().enrichHistoricDecisionsWithInputsAndOutputs(query,
        decisionInstances);

    return decisionInstances;
  }

  private void checkIsAuthorizedToReadHistoryOfDecisionDefinitions() {
    getAuthorizationManager().checkAuthorization(READ_HISTORY, DECISION_DEFINITION);
  }

}
