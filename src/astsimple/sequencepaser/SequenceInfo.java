package astsimple.sequencepaser;

import java.util.List;

public class SequenceInfo {
  public String sequenceName;
  public String MockedDependency;
  public String filePath;
  public String packages;
  public String testcase;
  public List<Sequence> sequences;

  // Constructors, getters, and setters are not shown for brevity

  public static class Sequence {
    private String rawStatement;
    private boolean isMockitoStatement;
    private String fixedFormat;
    // Constructors, getters, and setters are not shown for brevity
  }

  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    sb.append("  \"sequenceName\": \"").append(sequenceName).append("\",\n");
    sb.append("  \"MockedDependency\": \"").append(MockedDependency).append("\",\n");
    sb.append("  \"filePath\": \"").append(filePath).append("\",\n");
    sb.append("  \"packages\": \"").append(packages).append("\",\n");
    sb.append("  \"testcase\": \"").append(testcase).append("\",\n");
    sb.append("  \"sequences\": [\n");

    if (sequences != null) {
      for (int i = 0; i < sequences.size(); i++) {
        Sequence sequence = sequences.get(i);
        sb.append("    {\n");
        sb.append("      \"rawStatement\": \"").append(sequence.rawStatement).append("\",\n");
        sb.append("      \"isMockitoStatement\": ").append(sequence.isMockitoStatement)
          .append(",\n");
        sb.append("      \"fixedFormat\": \"").append(sequence.fixedFormat).append("\"\n");
        sb.append(i < sequences.size() - 1 ? "    },\n" : "    }\n");
      }
    }

    sb.append("  ]\n");
    sb.append("}");
    return sb.toString();
}
}