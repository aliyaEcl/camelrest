package ru.tests;

import org.junit.runners.Suite;
import org.junit.runners.Suite.*;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({IDTests.class, ValuesTests.class})
public class RestAPITestSuite{

}