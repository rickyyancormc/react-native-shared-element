package com.ijzerenhein.sharedelement;

import android.view.View;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.facebook.react.views.image.ReactImageView;
import com.facebook.react.views.view.ReactViewGroup;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.RoundingParams;

class RNSharedElementDrawable extends Drawable {
  enum ViewType {
    NONE("none"),
    REACTIMAGEVIEW("image"),
    IMAGEVIEW("image"),
    PLAIN("view"),
    GENERIC("generic");

    private final String value;

    ViewType(final String newValue) {
      value = newValue;
    }

    public String getValue() {
      return value;
    }
  }

  private RNSharedElementContent mContent = null;
  private RNSharedElementStyle mStyle = null;
  private ViewType mViewType = ViewType.NONE;
  private float mPosition = 0;
  private int mAlpha = 255;
  private Path mPathForBorderRadiusOutline = null;
  private ReactViewGroup mReactViewGroupCache = null;

  RNSharedElementStyle getStyle() {
    return mStyle;
  }

  RNSharedElementContent getContent() {
    return mContent;
  }

  float getPosition() {
    return mPosition;
  }

  ViewType update(
          RNSharedElementContent content,
          RNSharedElementStyle style,
          float position
  ) {
    boolean invalidated = false;

    if (mContent != content) {
      mContent = content;
      invalidated = true;
    }

    ViewType viewType = (mContent != null) ? RNSharedElementDrawable.getViewType(mContent.view, style) : ViewType.NONE;
    if (mViewType != viewType) {
      mViewType = viewType;
      invalidated = true;
    }

    if ((mStyle != null) && (style != null) && !invalidated) {
      switch (viewType) {
        case REACTIMAGEVIEW:
        case IMAGEVIEW:
          invalidated = (mStyle.compare(style) &
                  (RNSharedElementStyle.PROP_BORDER
                          | RNSharedElementStyle.PROP_BACKGROUND_COLOR
                          | RNSharedElementStyle.PROP_SCALETYPE)) != 0;
          break;
        case PLAIN:
          invalidated = (mStyle.compare(style) &
                  (RNSharedElementStyle.PROP_BORDER
                          | RNSharedElementStyle.PROP_BACKGROUND_COLOR)) != 0;
          break;
        case GENERIC:
          break;
      }
    }
    mStyle = style;
    mPosition = position;

    if (invalidated) {
      invalidateSelf();
    }

    return viewType;
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
  }

  @Override
  public void setAlpha(int alpha) {
    if (alpha != mAlpha) {
      mAlpha = alpha;
    }
  }

  @Override
  public int getAlpha() {
    return mAlpha;
  }

  @Override
  public void getOutline(Outline outline) {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
      super.getOutline(outline);
      return;
    }
    if (mStyle == null) {
      outline.setRect(getBounds());
      return;
    }
    if ((mStyle.borderTopLeftRadius == 0) &&
            (mStyle.borderTopRightRadius == 0) &&
            (mStyle.borderBottomLeftRadius == 0) &&
            (mStyle.borderBottomRightRadius == 0)) {
      outline.setRect(getBounds());
      return;
    }

    if (mPathForBorderRadiusOutline == null) {
      mPathForBorderRadiusOutline = new Path();
    } else {
      mPathForBorderRadiusOutline.reset();
    }
    float extraRadiusForOutline = mStyle.borderWidth / 2f;
    mPathForBorderRadiusOutline.addRoundRect(
            new RectF(getBounds()),
            new float[]{
                    mStyle.borderTopLeftRadius + extraRadiusForOutline,
                    mStyle.borderTopLeftRadius + extraRadiusForOutline,
                    mStyle.borderTopRightRadius + extraRadiusForOutline,
                    mStyle.borderTopRightRadius + extraRadiusForOutline,
                    mStyle.borderBottomRightRadius + extraRadiusForOutline,
                    mStyle.borderBottomRightRadius + extraRadiusForOutline,
                    mStyle.borderBottomLeftRadius + extraRadiusForOutline,
                    mStyle.borderBottomLeftRadius + extraRadiusForOutline
            },
            Path.Direction.CW
    );
    outline.setConvexPath(mPathForBorderRadiusOutline);
  }

  static ViewType getViewType(View view, RNSharedElementStyle style) {
    if (view == null) return ViewType.NONE;
    if (view instanceof ReactImageView) {
      return ViewType.REACTIMAGEVIEW;
    } else if (view instanceof ImageView) {
      return ViewType.IMAGEVIEW;
    } else if (view instanceof ReactViewGroup) {
      ReactViewGroup viewGroup = (ReactViewGroup) view;
      if (viewGroup.getChildCount() == 0) {
        if (style.isVisible()) {
          return ViewType.PLAIN;
        } else {
          return ViewType.NONE;
        }
      }
    }
    return ViewType.GENERIC;
  }

  @Override
  public void draw(Canvas canvas) {
    switch (mViewType) {
      case REACTIMAGEVIEW:
        drawReactImageView(canvas);
        break;
      case IMAGEVIEW:
        drawImageView(canvas);
        break;
      case PLAIN:
        drawPlainView(canvas);
        break;
      case GENERIC:
        drawGenericView(canvas);
        break;
    }
  }

  private void drawReactImageView(Canvas canvas) {
    ReactImageView imageView = (ReactImageView) mContent.view;
    RNSharedElementStyle style = mStyle;
    GenericDraweeHierarchy hierarchy = imageView.getHierarchy();
    Drawable drawable = hierarchy.getTopLevelDrawable();
    if (drawable == null) return;

    Rect oldBounds = new Rect(drawable.getBounds());
    ScaleType oldScaleType = hierarchy.getActualImageScaleType();
    RoundingParams oldRoundingParams = hierarchy.getRoundingParams();
    Drawable oldBackgroundImage = null;
    int oldFadeDuration = hierarchy.getFadeDuration();

    drawable.setBounds(getBounds());
    hierarchy.setActualImageScaleType(style.scaleType);
    RoundingParams roundingParams = new RoundingParams();
    roundingParams.setBorderColor(style.borderColor);
    roundingParams.setBorderWidth(style.borderWidth);
    roundingParams.setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY);
    roundingParams.setCornersRadii(
            style.borderTopLeftRadius,
            style.borderTopRightRadius,
            style.borderBottomRightRadius,
            style.borderBottomLeftRadius
    );
    hierarchy.setRoundingParams(roundingParams);
    hierarchy.setBackgroundImage(null);
    hierarchy.setFadeDuration(0);

    drawable.draw(canvas);

    hierarchy.setFadeDuration(oldFadeDuration);
    hierarchy.setBackgroundImage(oldBackgroundImage);
    hierarchy.setRoundingParams(oldRoundingParams);
    hierarchy.setActualImageScaleType(oldScaleType);
    drawable.setBounds(oldBounds);
  }

  private void drawImageView(Canvas canvas) {
    ImageView imageView = (ImageView) mContent.view;
    RNSharedElementStyle style = mStyle;
    Drawable drawable = imageView.getDrawable();
    if (drawable == null) return;

    Rect oldBounds = new Rect(drawable.getBounds());

    int width = (int) mContent.size.right;
    int height = (int) mContent.size.bottom;
    drawable.setBounds(0, 0, width, height);
    Matrix matrix = new Matrix();
    style.scaleType.getTransform(matrix, getBounds(), width, height, 0.5f, 0.5f);

    int saveCount = canvas.save();
    canvas.concat(matrix);
    drawable.draw(canvas);
    canvas.restoreToCount(saveCount);

    drawable.setBounds(oldBounds);
  }

  private void drawPlainView(Canvas canvas) {
    RNSharedElementStyle style = mStyle;

    ReactViewGroup viewGroup = mReactViewGroupCache;
    if (viewGroup == null) {
      viewGroup = new ReactViewGroup(mContent.view.getContext());
      mReactViewGroupCache = viewGroup;
    }
    viewGroup.setBackgroundColor(style.backgroundColor);

    viewGroup.draw(canvas);
  }

  private void drawGenericView(Canvas canvas) {
    mContent.view.draw(canvas);
  }
}
