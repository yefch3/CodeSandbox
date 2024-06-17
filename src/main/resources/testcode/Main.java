public class Main {
    public static void main(String[] args) {
        // 检查是否提供了两个参数
        if (args.length != 2) {
            System.out.println("Please provide exactly two numbers as arguments.");
            return;
        }

        try {
            // 将字符串参数转换为整数
            int num1 = Integer.parseInt(args[0]);
            int num2 = Integer.parseInt(args[1]);

            // 计算和
            int sum = num1 + num2;

            // 输出结果
            System.out.println("The sum of " + num1 + " and " + num2 + " is: " + sum);
        } catch (NumberFormatException e) {
            // 捕获并处理可能的格式异常
            System.out.println("Invalid number format. Please provide valid integers.");
        }
    }
}
