/**
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.zjsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public final class JsonPatch {

  private static final DecodePathFunction DECODE_PATH_FUNCTION = new DecodePathFunction();

  private JsonPatch() {}

  private final static class DecodePathFunction implements Function<String, String> {
    @Override
    public String apply(String path) {
      return path.replaceAll("~1", "/").replaceAll("~0", "~"); // see http://tools.ietf.org/html/rfc6901#section-4
    }
  }

  private static JsonNode getPatchAttr(JsonNode jsonNode, String attr) {
    JsonNode child = jsonNode.get(attr);
    if (child == null)
      throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
    return child;
  }

  private static JsonNode getPatchAttrWithDefault(JsonNode jsonNode, String attr, JsonNode defaultValue) {
    JsonNode child = jsonNode.get(attr);
    if (child == null)
      return defaultValue;
    else
      return child;
  }

  private static void process(JsonNode patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
      throws InvalidJsonPatchException {

    if (!patch.isArray())
      throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
    Iterator<JsonNode> operations = patch.iterator();
    while (operations.hasNext()) {
      JsonNode jsonNode = operations.next();
      if (!jsonNode.isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
      Operation operation = Operation.fromRfcName(getPatchAttr(jsonNode, Constants.OP).toString().replaceAll("\"", ""));
      List<String> path = getPath(getPatchAttr(jsonNode, Constants.PATH));

      switch (operation) {
        case REMOVE: {
          processor.remove(path);
          break;
        }

        case ADD: {
          JsonNode value;
          if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
            value = getPatchAttr(jsonNode, Constants.VALUE);
          else
            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
          processor.add(path, value);
          break;
        }

        case REPLACE: {
          JsonNode value;
          if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
            value = getPatchAttr(jsonNode, Constants.VALUE);
          else
            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
          processor.replace(path, value);
          break;
        }

        case MOVE: {
          List<String> fromPath = getPath(getPatchAttr(jsonNode, Constants.FROM));
          processor.move(fromPath, path);
          break;
        }

        case COPY: {
          List<String> fromPath = getPath(getPatchAttr(jsonNode, Constants.FROM));
          processor.copy(fromPath, path);
          break;
        }

        case TEST: {
          JsonNode value;
          if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
            value = getPatchAttr(jsonNode, Constants.VALUE);
          else
            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, NullNode.getInstance());
          processor.test(path, value);
          break;
        }
      }
    }
  }

  public static void validate(JsonNode patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
    process(patch, NoopProcessor.INSTANCE, flags);
  }

  public static void validate(JsonNode patch) throws InvalidJsonPatchException {
    validate(patch, CompatibilityFlags.defaults());
  }

  public static JsonNode apply(JsonNode patch, JsonNode source, EnumSet<CompatibilityFlags> flags) throws JsonPatchApplicationException {
    ApplyProcessor processor = new ApplyProcessor(source);
    process(patch, processor, flags);
    return processor.result();
  }

  public static JsonNode apply(JsonNode patch, JsonNode source) throws JsonPatchApplicationException {
    return apply(patch, source, CompatibilityFlags.defaults());
  }

  private static List<String> getPath(JsonNode path) {
    String pathString = path.toString().replaceAll("\"", "");
    List<String> pathList = Stream.of(pathString)
        .map(w -> w.split("/")).flatMap(Arrays::stream)
        .map(DECODE_PATH_FUNCTION)
        .collect(Collectors.toList());
    if (pathList.isEmpty()) {
      return Arrays.asList("", "");
    }
    if (pathString.endsWith("/")) {
      pathList.add("");
    }
    return pathList;
  }
}
