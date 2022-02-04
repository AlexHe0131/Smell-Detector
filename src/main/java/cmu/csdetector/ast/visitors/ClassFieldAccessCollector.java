package cmu.csdetector.ast.visitors;

import cmu.csdetector.ast.CollectorVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
/**
 * Visit a method body to find all accesses to class fields
 * During the SimpleName visit; This visitor uses binding to determine
 * if the simple name (ASTnode) is a field in the class or not
 * If so, we save that node
 */
public class ClassFieldAccessCollector extends CollectorVisitor<IVariableBinding> {
    private Set<IVariableBinding> allVariables; // save all variables that are class field

    /**
     * Type that declares the method being visited
     */
    private ITypeBinding declaringTypeBinding;

    public ClassFieldAccessCollector(TypeDeclaration declaringType) {
        this.declaringTypeBinding = declaringType.resolveBinding(); // resolve binding
        this.allVariables = this.getVariablesInHierarchy();
    }

    public boolean visit(SimpleName node) {

        if(this.declaringTypeBinding == null) {
            return false;
        }

        IBinding binding = node.resolveBinding();
        if(binding == null) {
            return false;
        }

        // we can check to see if the node is a variable
        if(binding.getKind() == IBinding.VARIABLE) {
            IVariableBinding variableBinding = (IVariableBinding) binding;
            if(!wasAlreadyCollected(variableBinding) && allVariables.contains(variableBinding)) {
                addCollectedNode(variableBinding);
            }
        }

        return true;
    }

    private Set<IVariableBinding> getVariablesInHierarchy() {
        // variables that are class fields (attributes
        Set<IVariableBinding> variables = new HashSet<>();
        ITypeBinding type = this.declaringTypeBinding; // this represents a class

        while(type != null) {
            IVariableBinding[] localVariables = type.getDeclaredFields();
            variables.addAll(Arrays.asList(localVariables));

            type = type.getSuperclass();
        }
        return variables;
    }
}

