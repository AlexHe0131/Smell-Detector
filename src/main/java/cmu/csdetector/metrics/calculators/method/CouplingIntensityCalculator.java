package cmu.csdetector.metrics.calculators.method;

import cmu.csdetector.ast.visitors.DistinctMethodInvocationVisitor;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.Map;
import java.util.Set;

/**
 * The number of calls for distinct methods that a class did
 * @author Diego Cedrim
 */
public class CouplingIntensityCalculator extends MetricValueCalculator {
	
	protected Map<ITypeBinding, Set<IMethodBinding>> methodCalls;
	
	@Override
	protected Double computeValue(ASTNode target) {
		this.methodCalls = getMethodCalls(target);
		if (this.methodCalls == null) {
			return 0d;
		}
		
		Double cint = 0d;
		for (Set<IMethodBinding> distinctCalls : this.methodCalls.values() ) {
			cint += distinctCalls.size();
		}
		return cint;
	}

	private Map<ITypeBinding, Set<IMethodBinding>> getMethodCalls(ASTNode target) {
		MethodDeclaration method = (MethodDeclaration)target;
		IMethodBinding binding = method.resolveBinding();
		if (binding == null) {
			return null;
		}
		ITypeBinding clazz = binding.getDeclaringClass();
		DistinctMethodInvocationVisitor visitor = new DistinctMethodInvocationVisitor(clazz);
		target.accept(visitor);
		
		return visitor.getMethodsCalls();
	}

	@Override
	public MetricName getMetricName() {
		return MetricName.CINT;
	}
	
	@Override
	public boolean shouldComputeAggregate() {
		return true;
	}
	
}
