/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.zjsonpatch.internal.collections4.sequence;

/**
 * Command representing the keeping of one object present in both sequences.
 * <p>
 * When one object of the first sequence <code>equals</code> another objects in
 * the second sequence at the right place, the {@link EditScript edit script}
 * transforming the first sequence into the second sequence uses an instance of
 * this class to represent the keeping of this object. The objects embedded in
 * these type of commands always come from the first sequence.
 *
 * @version $Id: KeepCommand.html 887892 2013-11-24 13:43:45Z tn $
 * @see SequencesComparator
 * @see EditScript
 * @since 4.0
 */
public class KeepCommand<T> extends EditCommand<T> {

  /**
   * Simple constructor. Creates a new instance of KeepCommand
   *
   * @param object the object belonging to both sequences (the object is a
   *               reference to the instance in the first sequence which is known
   *               to be equal to an instance in the second sequence)
   */
  public KeepCommand(final T object) {
    super(object);
  }

  /**
   * Accept a visitor. When a <code>KeepCommand</code> accepts a visitor, it
   * calls its {@link CommandVisitor#visitKeepCommand visitKeepCommand} method.
   *
   * @param visitor the visitor to be accepted
   */
  @Override
  public void accept(final CommandVisitor<T> visitor) {
    visitor.visitKeepCommand(getObject());
  }
}