package astsimple.handlers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.Annotation;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;

public class ImportVisitor extends ASTVisitor{
	
	List<ImportDeclaration> imports = new ArrayList<>();
	
	List<String> mocked_with_annotation = new ArrayList<>();

	@Override
	public boolean visit(ImportDeclaration node) {
        imports.add(node);
        return super.visit(node);
    }

    public List<ImportDeclaration> getImportDeclarations() {
        return imports;
    }
    
    @Override
	public boolean visit(FieldDeclaration node) {
    	
    	for(Object x: node.modifiers()) {
    		if(x instanceof Annotation) {
    			Annotation annotation = (Annotation)x;
    			
    			if(node.getType().resolveBinding()!= null && annotation.resolveTypeBinding().getQualifiedName().toLowerCase().contains("mock")) {
    				System.out.println(annotation.resolveTypeBinding().getQualifiedName());
    				System.out.println(node.getType().resolveBinding().getQualifiedName());
        			System.out.println(node);
        			mocked_with_annotation.add(node.getType().resolveBinding().getQualifiedName());
    			}
    			
    			
    			
    			
    		}
    		
    		
//    		System.out.println(x.getClass() + "" + x);
    		
    	}
    	
    	
//    	System.out.println(node.modifiers());
//        imports.add(node);
        return super.visit(node);
    }
    
    public List<String> getAnnotationMocks() {
        return mocked_with_annotation;
    }
    
    
    
    
    
    
    
    
    
    
//    List<MethodInvocation> methodInvocation = new ArrayList<>();
//    
//    @Override
//    public boolean visit(MethodInvocation node) {
//    	
//    	methodInvocation.add(node);
//    	return super.visit(node);
//    }
//    
//    public List<MethodInvocation> getMethodInvocations(){
//    	return methodInvocation;
//    }

}
