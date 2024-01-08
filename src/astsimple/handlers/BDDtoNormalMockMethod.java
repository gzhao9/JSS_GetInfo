package astsimple.handlers;

import java.util.HashMap;
import java.util.Map;

public class BDDtoNormalMockMethod {
  static public String setStubberToNormal(String method) {
    Map<String, String> bddMockitoToMockitoMap = new HashMap<>();


    bddMockitoToMockitoMap.put("willAnswer", "doAnswer");
    bddMockitoToMockitoMap.put("willCallRealMethod", "doCallRealMethod");
    bddMockitoToMockitoMap.put("willNothing", "doNothing");
    bddMockitoToMockitoMap.put("willReturn", "doReturn");
    bddMockitoToMockitoMap.put("willThrow", "doThrow");

    if (bddMockitoToMockitoMap.containsKey(method)) {
      return bddMockitoToMockitoMap.get(method);
    }
    return method;
  }

  static public String setBDDMyOngoingToNormal(String method) {
    Map<String, String> bddMockitoToMockitoMap = new HashMap<>();

    bddMockitoToMockitoMap.put("will", "then");
    bddMockitoToMockitoMap.put("willAnswer", "thenAnswer");
    bddMockitoToMockitoMap.put("willCallRealMethod", "thenCallRealMethod");
    bddMockitoToMockitoMap.put("willReturn", "thenReturn");
    bddMockitoToMockitoMap.put("willThrow", "thenThrow");
    if (bddMockitoToMockitoMap.containsKey(method)) {
      return bddMockitoToMockitoMap.get(method);
    }
    return method;
  }
}
