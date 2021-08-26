import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class App {
	static public class ValueNode implements INode {
		private long value;

		public ValueNode(long value) {
			this.value = value;
		}

		public TreeMap<Long, Float> Calculate() {
			TreeMap<Long, Float> result = new TreeMap<Long, Float>();
			result.put(value, 1.0f);
			return result;
		}
	}

	static public class DiceNode implements INode {
		private Long faceCount;

		public DiceNode(Long faceCount) {
			this.faceCount = faceCount;
		}

		public TreeMap<Long, Float> Calculate() {
			TreeMap<Long, Float> result = new TreeMap<Long, Float>();
			
			float prohibitance = 1.0f / this.faceCount;
			for(Long i = 1l; i <= faceCount; i++){
				result.put(i, prohibitance);
			}

			return result;
		}
		
	}

	static public class OperatorNode implements INode {
		private char operator;
		private INode leftNode;
		private INode rightNode;
		private int priority;

		public OperatorNode(
			char operator,
			int priority) {
			this.operator = operator;
			this.priority = priority;
		}

		public void SetLeftNode(INode leftNode) {
			this.leftNode = leftNode;
		}

		public void SetRightNode(INode rightNode) {
			this.rightNode = rightNode;
		}

		public int GetPriority(){
			return this.priority;
		}
		
		public TreeMap<Long, Float> Calculate() {
			TreeMap<Long, Float> leftResult = this.leftNode.Calculate();
			TreeMap<Long, Float> rightResult = this.rightNode.Calculate();
			TreeMap<Long, Float> result = new TreeMap<Long, Float>();
			
			for (Map.Entry<Long, Float> lr : leftResult.entrySet()) {
				for (Map.Entry<Long, Float> rh : rightResult.entrySet()) {
					Long newValue = 0l;

					if (operator == '+'){
						newValue = lr.getKey() + rh.getKey();
					} else if (operator == '*'){
						newValue = lr.getKey() * rh.getKey();
					}  else if (operator == '-'){
						newValue = lr.getKey() - rh.getKey();
					} else if (operator == '>') {
						if (lr.getKey() > rh.getKey()){
							newValue = 1l;
						}
					}
					
					float prohibitance = lr.getValue() * rh.getValue();

					if (!result.containsKey(newValue)){
						result.put(newValue, prohibitance);
					} else {
						result.put(newValue, result.get(newValue) + prohibitance);
					}
				}
			}
			
			return result;
		}
	}

	static public interface INode {
		public TreeMap<Long, Float> Calculate();
	}

	static public class TreeExpression {
		private ArrayList<INode> expressionList = new ArrayList<INode>();
		private Stack<OperatorNode> lastOperations = new Stack<OperatorNode>();
		private OperatorNode header;
		
		public TreeMap<Long, Float> Calculate() {
			return header.Calculate();
		}

		public void AddToTree(String strNumber, char operation, boolean isDice, int priority) {
			Long number = (long)Integer.parseInt(strNumber);
			INode node = null;
			if (isDice) {
				node = new DiceNode(number);
			} else {
				node = new ValueNode(number);
			}
			expressionList.add(node);
			OperatorNode operator = new OperatorNode(operation, priority);

			if (!lastOperations.empty()) {
				var lastOperator = lastOperations.peek();

				if (lastOperator.GetPriority() > operator.GetPriority()) {
					expressionList.add(lastOperations.pop()); 
				} 
			}

			lastOperations.push(operator);
		}

		public void AddLastValue(String strNumber, boolean isDice) {
			Long number = (long)Integer.parseInt(strNumber);
			INode node = null;
			if (isDice) {
				node = new DiceNode(number);
				isDice = false;
			} else {
				node = new ValueNode(number);
			}

			expressionList.add(node);

			while (!lastOperations.empty()) {
				expressionList.add(lastOperations.pop());
			}

			int i = 2;
			while (i < expressionList.size()) {
				var opeartor = (expressionList.get(i) instanceof OperatorNode ? (OperatorNode)expressionList.get(i) : null);

				if (opeartor != null){
					opeartor.SetLeftNode(expressionList.get(i-2));
					opeartor.SetRightNode(expressionList.get(i-1));

					expressionList.remove(i-1);
					expressionList.remove(i-2);
					i--;
				} else {
					i++;
				}
			}

			header = (OperatorNode)expressionList.get(0);
		}
	}

	public static void main(String[] args) {
		java.util.Scanner in = new java.util.Scanner(System.in);
		String line = in.nextLine();
		TreeExpression expression = ParseExpression(line);
		TreeMap<Long, Float> result = expression.Calculate();

		for (Map.Entry<Long, Float> m : result.entrySet()) {
			System.out.println(m.getKey() + " " + String.format(Locale.US, "%.2f", (float)Math.round(m.getValue()*100*100)/100));
		}
	}

    private static TreeExpression ParseExpression(String expresion){
		var arrExpression = expresion.toCharArray();
		var treeExpression = new TreeExpression();

		String strNumber = "";
		boolean isDice = false;
		int priority = 0;

		for(int i = 0; i < arrExpression.length; i++){
			char c = arrExpression[i];

			if (Character.isDigit(c)) {
				strNumber += arrExpression[i];
			} else if (c == '+' || c == '-') {
				treeExpression.AddToTree(strNumber, c, isDice, priority + 3);
				strNumber = "";
				isDice = false;
			} else if (c == '*') {
				treeExpression.AddToTree(strNumber, c, isDice, priority + 5);
				strNumber = "";
				isDice = false;
			} else if (c == '>') {
				treeExpression.AddToTree(strNumber, c, isDice, priority);
				strNumber = "";
				isDice = false;
			} else if (c == ')') {
				priority -= 10;
				isDice = false;
			} else if (c == '(') {
				priority += 10;
				isDice = false;
			} else if (c == 'd') {
				isDice = true;
			}
		}

		treeExpression.AddLastValue(strNumber, isDice);

		return treeExpression;
	}
}
