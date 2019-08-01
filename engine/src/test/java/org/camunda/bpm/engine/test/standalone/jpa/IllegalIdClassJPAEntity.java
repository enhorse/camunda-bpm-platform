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
package org.camunda.bpm.engine.test.standalone.jpa;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 
 * WARNING: This class cannot be used in JPA-context, since it has an illegal type of ID.
 * 
 * For testing purposes only.
 * 
 * @author Frederik Heremans
 */
@Entity
public class IllegalIdClassJPAEntity {

  @Id
  private Calendar id;

  public Calendar getId() {
    return id;
  }

  public void setId(Calendar id) {
    this.id = id;
  }

}
