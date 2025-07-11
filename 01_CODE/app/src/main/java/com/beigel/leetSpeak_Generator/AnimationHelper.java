package com.beigel.leetSpeak_Generator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class AnimationHelper {
    private static final int ANIMATION_DURATION_SHORT = 200;
    private static final int ANIMATION_DURATION_MEDIUM = 300;
    private static final int ANIMATION_DURATION_LONG = 500;

    private static final Interpolator EASE_IN = new AccelerateInterpolator();
    private static final Interpolator EASE_OUT = new DecelerateInterpolator();
    private static final Interpolator EASE_IN_OUT = new AccelerateDecelerateInterpolator();

    public static void slideInFromBottom(View view) {
        slideInFromBottom(view, ANIMATION_DURATION_MEDIUM, null);
    }

    public static void slideInFromBottom(View view, int duration, Runnable onComplete) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(EASE_OUT)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (onComplete != null) onComplete.run();
                    }
                })
                .start();
    }

    public static void slideOutToBottom(View view, Runnable onComplete) {
        view.animate()
                .translationY(view.getHeight())
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_MEDIUM)
                .setInterpolator(EASE_IN)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        view.setTranslationY(0);
                        if (onComplete != null) onComplete.run();
                    }
                })
                .start();
    }

    public static void crossFade(View viewOut, View viewIn) {
        crossFade(viewOut, viewIn, ANIMATION_DURATION_MEDIUM, null);
    }

    public static void crossFade(View viewOut, View viewIn, int duration, Runnable onComplete) {
        viewIn.setAlpha(0f);
        viewIn.setVisibility(View.VISIBLE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(viewOut, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(viewIn, "alpha", 0f, 1f)
        );
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(EASE_IN_OUT);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewOut.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            }
        });
        animatorSet.start();
    }

    public static void scaleButton(View button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(EASE_IN)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .setInterpolator(EASE_OUT)
                                .setListener(null)
                                .start();
                    }
                })
                .start();
    }

    public static void expandCard(View card, Runnable onComplete) {
        card.setVisibility(View.VISIBLE);

        card.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int targetHeight = card.getMeasuredHeight();

        card.getLayoutParams().height = 0;
        card.requestLayout();

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(ANIMATION_DURATION_MEDIUM);
        animator.setInterpolator(EASE_OUT);
        animator.addUpdateListener(animation -> {
            int height = (Integer) animation.getAnimatedValue();
            card.getLayoutParams().height = height;
            card.requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                card.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (onComplete != null) onComplete.run();
            }
        });
        animator.start();
    }

    public static void collapseCard(View card, Runnable onComplete) {
        int initialHeight = card.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(ANIMATION_DURATION_MEDIUM);
        animator.setInterpolator(EASE_IN);
        animator.addUpdateListener(animation -> {
            int height = (Integer) animation.getAnimatedValue();
            card.getLayoutParams().height = height;
            card.requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                card.setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
            }
        });
        animator.start();
    }

    public static void staggeredFadeIn(ViewGroup container, int delayBetweenItems) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(50f);

            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION_MEDIUM)
                    .setStartDelay((long) i * delayBetweenItems)
                    .setInterpolator(EASE_OUT)
                    .start();
        }
    }

    // KORRIGIERTE pulse Methode - verwendet ObjectAnimator statt AnimatorSet
    public static void pulse(View view, int repeatCount) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        // Setze Repeat Count auf den einzelnen Animatoren
        scaleX.setRepeatCount(repeatCount);
        scaleY.setRepeatCount(repeatCount);
        scaleX.setDuration(ANIMATION_DURATION_SHORT * 2);
        scaleY.setDuration(ANIMATION_DURATION_SHORT * 2);
        scaleX.setInterpolator(EASE_IN_OUT);
        scaleY.setInterpolator(EASE_IN_OUT);

        // Starte beide Animationen gleichzeitig
        scaleX.start();
        scaleY.start();
    }

    // Alternative pulse Methode mit AnimatorSet (ohne Repeat)
    public static void pulseOnce(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(ANIMATION_DURATION_SHORT * 2);
        animatorSet.setInterpolator(EASE_IN_OUT);
        animatorSet.start();
    }

    // Erweiterte pulse Methode für mehrfache Wiederholung
    public static void pulseMultiple(View view, int repeatCount, Runnable onComplete) {
        if (repeatCount <= 0) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(ANIMATION_DURATION_SHORT * 2);
        animatorSet.setInterpolator(EASE_IN_OUT);

        final int[] currentRepeat = {0};
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentRepeat[0]++;
                if (currentRepeat[0] < repeatCount) {
                    // Starte nächste Wiederholung
                    animatorSet.start();
                } else {
                    // Animation beendet
                    if (onComplete != null) onComplete.run();
                }
            }
        });

        animatorSet.start();
    }
}