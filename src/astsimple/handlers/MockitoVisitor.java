package astsimple.handlers;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class MockitoVisitor extends ASTVisitor {
    private List<MockitoMockInfo> mockInfos;
    private String currentTestCase;

    public MockitoVisitor() {
        mockInfos = new ArrayList<>();
    }

    public List<MockitoMockInfo> getMockInfos() {
        return mockInfos;
    }  

    @Override
    public boolean visit(TypeDeclaration node) {
        if (node.resolveBinding().getQualifiedName().endsWith("Test")) {
            currentTestCase = node.getName().getIdentifier();
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getName().getIdentifier().equals("mock")) {
            String mockedObjectName = node.getParent().toString();
            String mockedClassName = node.arguments().get(0).toString();

            MockitoMockInfo mockInfo = new MockitoMockInfo();
            mockInfo.setMockedObjectName(mockedObjectName);
            mockInfo.setTestCaseName(currentTestCase);
            mockInfo.setMockedClassName(mockedClassName);

            mockInfos.add(mockInfo);
        }
        return super.visit(node);
    }
    public class MockitoMockInfo {
        private String mockedObjectName;
        private String fileName;
        private String testCaseName;
        private String mockedClassName;
        public String getMockedObjectName() {
            return mockedObjectName;
        }

        public void setMockedObjectName(String mockedObjectName) {
            this.mockedObjectName = mockedObjectName;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getTestCaseName() {
            return testCaseName;
        }

        public void setTestCaseName(String testCaseName) {
            this.testCaseName = testCaseName;
        }

        public String getMockedClassName() {
            return mockedClassName;
        }

        public void setMockedClassName(String mockedClassName) {
            this.mockedClassName = mockedClassName;
        }
    }
}