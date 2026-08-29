//package org.example.fixfw.listeners;
//
//import com.aventstack.extentreports.ExtentTest;
//import org.testng.ITestContext;
//import org.testng.ITestListener;
//import org.testng.ITestResult;
//
//public class ExtentListener implements ITestListener {
//
//    private static ExtentTest test;
//
//    public void onTestStart(ITestResult result) {
//        test = ExtentManager.get().createTest(result.getName());
//    }
//
//    public void onTestSuccess(ITestResult result) {
//        test.pass("Test Passed");
//    }
//
//    public void onTestFailure(ITestResult result) {
//        test.fail(result.getThrowable());
//    }
//
//    public void onFinish(ITestContext context) {
//        ExtentManager.get().flush();
//    }
//}
//
