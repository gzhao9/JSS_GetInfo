package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;

public class SequenceInfo {
  public String sequenceName;
  public String mockedDependency;
  public String valuetypeName;
  public String filePath;
  public String packages;
  public String testcase;
  public String creationStatement;

  public List<String> annotationsName;
  public List<MockSequence> sequences;

  public SequenceInfo() {
    this.sequenceName = "";
    this.mockedDependency = "";
    this.filePath = "";
    this.packages = "";
    this.testcase = "";
    this.creationStatement = "";
    this.annotationsName = new ArrayList<>(); // Initialized to an empty list
    this.sequences = new ArrayList<>();
  }

  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    sb.append("  \"sequenceName\": \"").append(sequenceName).append("\",\n");
    sb.append("  \"creationStatement\": \"").append(creationStatement).append("\",\n");
    sb.append("  \"valuetypeName\": \"").append(valuetypeName).append("\",\n");
    sb.append("  \"MockedDependency\": \"").append(mockedDependency).append("\",\n");
    sb.append("  \"filePath\": \"").append(filePath).append("\",\n");
    sb.append("  \"packages\": \"").append(packages).append("\",\n");
    sb.append("  \"testcase\": \"").append(testcase).append("\",\n");

    sb.append("  \"testcaseAnnotates\": [\n");
    if (annotationsName != null) {
      for (int i = 0; i < annotationsName.size(); i++) {
        String annotation = annotationsName.get(i);
        sb.append("     \"").append(annotation);
        sb.append(i < annotationsName.size() - 1 ? "\",\n" : "\"\n");
      }
    }
    sb.append("  ],\n");

    sb.append("  \"sequencesDteial\": [\n");
    if (sequences != null) {
      for (int i = 0; i < sequences.size(); i++) {
        MockSequence sequence = sequences.get(i);
        sb.append("    {\n");
        sb.append("      \"rawStatement\": \"").append(sequence.rawStatement).append("\",\n");
        sb.append("      \"isMockRelatedStatements\": ").append(sequence.isMockRelatedStatements)
          .append(",\n");
        sb.append("      \"fixedFormat\": \"").append(sequence.fixedFormat).append("\",\n");
        sb.append("      \"readToDecting\": \"").append(sequence.readToDecting).append("\"\n");
        sb.append(i < sequences.size() - 1 ? "    },\n" : "    }\n");
      }
    }
    sb.append("  ],\n");

    sb.append("  \"statements\": [\n");
    if (sequences != null) {
      for (int i = 0; i < sequences.size(); i++) {
        MockSequence sequence = sequences.get(i);
        sb.append("     \"").append(sequence.rawStatement);
        sb.append(i < sequences.size() - 1 ? "\",\n" : "\"\n");
      }
    }
    sb.append("  ],\n");

    sb.append("  \"mockSequences\": [\n");
    if (sequences != null) {
      List<String> mockSequences = new ArrayList<>();
      for (MockSequence sequence : sequences) {
        if (sequence.isMockRelatedStatements) {
          mockSequences.add(sequence.rawStatement);
        }
      }
      for (int i = 0; i < mockSequences.size(); i++) {
        String sequence = mockSequences.get(i);
        sb.append("     \"").append(sequence);
        sb.append(i < mockSequences.size() - 1 ? "\",\n" : "\"\n");

      }
    }
    sb.append("  ],\n");
    sb.append("  \"fixedmockSequences\": [\n");
    if (sequences != null) {
      List<String> fixedSequences = new ArrayList<>();
      for (MockSequence sequence : sequences) {
        if (sequence.isMockRelatedStatements && (sequence.fixedFormat.length() > 0)) {
          fixedSequences.add(sequence.fixedFormat);
        }
      }
      for (int i = 0; i < fixedSequences.size(); i++) {
        String sequence = fixedSequences.get(i);
        sb.append("     \"").append(sequence);
        sb.append(i < fixedSequences.size() - 1 ? "\",\n" : "\"\n");

      }
    }
    sb.append("  ],\n");
    sb.append("  \"readToDecting\": [\n");
    if (sequences != null) {
      List<String> readToDecting = new ArrayList<>();
      for (MockSequence sequence : sequences) {
        if (sequence.isMockRelatedStatements && (sequence.readToDecting.length() > 0)) {
          readToDecting.add(sequence.readToDecting);
        }
      }
      for (int i = 0; i < readToDecting.size(); i++) {
        String sequence = readToDecting.get(i);
        sb.append("     \"").append(sequence);
        sb.append(i < readToDecting.size() - 1 ? "\",\n" : "\"\n");

      }
    }
    sb.append("  ]\n");
    sb.append("}");
    return sb.toString();
  }

  public static class MockSequence {
    private String rawStatement;
    private boolean isMockRelatedStatements;
    private String fixedFormat;
    private String readToDecting;

    public MockSequence() {
      this.rawStatement = "";
      this.isMockRelatedStatements = false;
      this.fixedFormat = "";
      this.readToDecting = "";
    }

    public MockSequence(String rawStatement, boolean isMockitoStatement, String fixedFormat,
      String readToDecting) {
      this.rawStatement = rawStatement;
      this.isMockRelatedStatements = isMockitoStatement;
      this.fixedFormat = fixedFormat;
      this.readToDecting = readToDecting;
    }
  }
}