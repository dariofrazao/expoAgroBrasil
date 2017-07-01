package com.expoagro.expoagrobrasil.view;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.expoagro.expoagrobrasil.R;
import com.expoagro.expoagrobrasil.controller.LoginActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4.class)
public class LoginActivityTestEmailInvalido {

    private Activity currentActivity;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void unlockScreen() {
        final LoginActivity activity = mActivityTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void loginActivityTest() throws Exception{
        onView(withId(R.id.email)).perform(typeText("diego.tester@teste.co"));
        closeKeyboard();
        onView(withId(R.id.password)).perform(typeText("123456"));
        closeKeyboard();
        onView(withId(R.id.btnEntrar)).perform(click());

        Thread.sleep(3000);
    }
//    @Test
//    public void loginActivityTest() {
//        try {
//            Thread.sleep(2000);
//            ViewInteraction appCompatAutoCompleteTextView = onView(
//                    allOf(withId(R.id.email), isDisplayed()));
//            appCompatAutoCompleteTextView.perform(click());
//
//            ViewInteraction appCompatAutoCompleteTextView2 = onView(
//                    allOf(withId(R.id.email), isDisplayed()));
//            appCompatAutoCompleteTextView2.perform(replaceText("diego.tester@teste.co"), closeSoftKeyboard());
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            Thread.sleep(2000);
//            ViewInteraction appCompatEditText = onView(
//                    allOf(withId(R.id.password), isDisplayed()));
//            appCompatEditText.perform(replaceText("senhateste"), closeSoftKeyboard());
//
//            ViewInteraction appCompatEditText2 = onView(
//                    allOf(withId(R.id.password), withText("senhateste"), isDisplayed()));
//            appCompatEditText2.perform(pressImeActionButton());
//
//            ViewInteraction appCompatButton = onView(
//                    allOf(withId(R.id.btnEntrar), withText("Entrar"),
////                        withParent(withId(R.id.email_login_form)),
//                            isDisplayed()));
//            appCompatButton.perform(click());
//
//            ViewInteraction viewGroup = onView(
//                    allOf(childAtPosition(
//                            allOf(withId(android.R.id.content),
//                                    childAtPosition(
//                                            withId(R.id.decor_content_parent),
//                                            0)),
//                            0),
//                            isDisplayed()));
//            viewGroup.check(matches(isDisplayed()));
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    private static Matcher<View> childAtPosition(
//            final Matcher<View> parentMatcher, final int position) {
//
//        return new TypeSafeMatcher<View>() {
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("Child at position " + position + " in parent ");
//                parentMatcher.describeTo(description);
//            }
//
//            @Override
//            public boolean matchesSafely(View view) {
//                ViewParent parent = view.getParent();
//                return parent instanceof ViewGroup && parentMatcher.matches(parent)
//                        && view.equals(((ViewGroup) parent).getChildAt(position));
//            }
//        };
//    }

    public Activity getActivityInstance() {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity;
    }

    public void closeKeyboard() throws Exception {
        Espresso.closeSoftKeyboard();
        Thread.sleep(1000);
    }
}