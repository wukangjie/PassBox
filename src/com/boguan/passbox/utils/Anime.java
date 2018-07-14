package com.boguan.passbox.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.boguan.passbox.R;

/**
 * Created by Lymons on 16/4/18.
 */
public class Anime {
    public interface Done {
        void ok(View v);
    }

    static Anime _instance;

    static Context mContext;

    private Anime(Context c) {
        mContext = c;
    }

    public static Anime init(Context c) {
        if (_instance == null) {
            _instance = new Anime(c);
        }
        return _instance;
    }

    public static void translateAnimation(View... vs) {
        translateAnimation(null, vs);
    }

    public static void translateAnimation(final Done done, View... vs) {
        int num = vs.length;
        for (int i = 0; i < num; i ++) {
            final View v = vs[i];
            final boolean last = (i == num - 1);
            v.setVisibility(View.INVISIBLE);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.entry_animation_icon);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.VISIBLE);
                    if (last && done != null) {
                        done.ok(v);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(animation);
        }
    }

    public static void translateAlphaInAnimation(int transNum, View... vs) {
        int len = vs.length;
        int left = transNum > len ? len : transNum;

        View[] trans = new View[left];
        for (int i = 0; i < left; i ++) {
            trans[i] = vs[i];
            vs[i].setVisibility(View.INVISIBLE);
        }

        if (left < len) {
            final View[] in = new View[len - left];
            for (int i = left; i < len; i ++) {
                in[i - left] = vs[i];
                vs[i].setVisibility(View.INVISIBLE);
            }

            translateAnimation(new Done() {
                @Override
                public void ok(View v) {
                    alphaInAnimation(null, in);
                }
            }, trans);
        } else {
            translateAnimation(trans);
        }
    }

    public static void alphaInAnimation(final Done done, View... vs) {
        int num = vs.length;
        for (int i = 0; i < num; i ++) {
            final View v = vs[i];
            v.setVisibility(View.INVISIBLE);
            final boolean last = (i == num - 1);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.entry_animation_alpha_from_0_to_1);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.VISIBLE);
                    if (last && done != null) {
                        done.ok(v);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(animation);
        }
    }

    public static void alphaOutAnimation(final Done done, View... vs) {
        int num = vs.length;
        for (int i = 0; i < num; i ++) {
            final View v = vs[i];
            v.setVisibility(View.VISIBLE);
            final boolean last = (i == num - 1);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.entry_animation_alpha_from_d_to_0);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.INVISIBLE);
                    if (last && done != null) {
                        done.ok(v);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(animation);
        }
    }

    public static void alphaOutInAnimation(final Done done, int outNum, View... vs) {
        int len = vs.length;
        int left = outNum > len ? len : outNum;

        View[] out = new View[left];
        for (int i = 0; i < left; i ++) {
            out[i] = vs[i];
            vs[i].setVisibility(View.VISIBLE);
        }

        if (left < len) {
            final View[] in = new View[len - left];
            for (int i = left; i < len; i ++) {
                in[i - left] = vs[i];
                vs[i].setVisibility(View.INVISIBLE);
            }

            alphaOutAnimation(new Done() {
                @Override
                public void ok(View v) {
                    alphaInAnimation(done, in);
                }
            }, out);
        } else {
            alphaOutAnimation(done, out);
        }
    }
}
