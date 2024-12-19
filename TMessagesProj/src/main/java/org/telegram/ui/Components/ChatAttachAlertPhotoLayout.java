/*
 * This is the source code of Telegram for Android v. 6.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */

package org.telegram.ui.Components;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.touchSlop;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.getString;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationNotificationsLocker;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.WindowViewAbstract;
import org.telegram.messenger.camera.CameraController;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.AvatarSpan;
import org.telegram.ui.BasePermissionsActivity;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Cells.PhotoAttachCameraCell;
import org.telegram.ui.Cells.PhotoAttachPermissionCell;
import org.telegram.ui.Cells.PhotoAttachPhotoCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.Paint.RenderView;
import org.telegram.ui.Components.Paint.Views.EntityView;
import org.telegram.ui.Components.Paint.Views.MessageEntityView;
import org.telegram.ui.Components.Paint.Views.PhotoView;
import org.telegram.ui.Components.Paint.Views.RoundView;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.Stories.StoriesController;
import org.telegram.ui.Stories.recorder.AlbumButton;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.Stories.recorder.CaptionStory;
import org.telegram.ui.Stories.recorder.CollageLayout;
import org.telegram.ui.Stories.recorder.CollageLayoutButton;
import org.telegram.ui.Stories.recorder.CollageLayoutView2;
import org.telegram.ui.Stories.recorder.DownloadButton;
import org.telegram.ui.Stories.recorder.DraftSavedHint;
import org.telegram.ui.Stories.recorder.DraftsController;
import org.telegram.ui.Stories.recorder.DualCameraView;
import org.telegram.ui.Stories.recorder.FlashViews;
import org.telegram.ui.Stories.recorder.GalleryListView;
import org.telegram.ui.Stories.recorder.HintTextView;
import org.telegram.ui.Stories.recorder.HintView2;
import org.telegram.ui.Stories.recorder.PaintView;
import org.telegram.ui.Stories.recorder.PhotoVideoSwitcherView;
import org.telegram.ui.Stories.recorder.PlayPauseButton;
import org.telegram.ui.Stories.recorder.PreviewButtons;
import org.telegram.ui.Stories.recorder.PreviewHighlightView;
import org.telegram.ui.Stories.recorder.PreviewView;
import org.telegram.ui.Stories.recorder.RecordControl;
import org.telegram.ui.Stories.recorder.RoundVideoRecorder;
import org.telegram.ui.Stories.recorder.SliderView;
import org.telegram.ui.Stories.recorder.StoryEntry;
import org.telegram.ui.Stories.recorder.StoryPrivacyBottomSheet;
import org.telegram.ui.Stories.recorder.StoryPrivacySelector;
import org.telegram.ui.Stories.recorder.StoryRecorder;
import org.telegram.ui.Stories.recorder.StoryThemeSheet;
import org.telegram.ui.Stories.recorder.TimelineView;
import org.telegram.ui.Stories.recorder.ToggleButton;
import org.telegram.ui.Stories.recorder.ToggleButton2;
import org.telegram.ui.Stories.recorder.TrashView;
import org.telegram.ui.Stories.recorder.VideoTimeView;
import org.telegram.ui.Stories.recorder.VideoTimerView;
import org.telegram.ui.WrappedResourceProvider;
import org.webrtc.EglBase14;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAttachAlertPhotoLayout extends ChatAttachAlert.AttachAlertLayout implements NotificationCenter.NotificationCenterDelegate {

    private static final int VIEW_TYPE_AVATAR_CONSTRUCTOR = 4;
    private static final int SHOW_FAST_SCROLL_MIN_COUNT = 30;
    private final boolean needCamera;
    private final int currentAccount = UserConfig.selectedAccount;

    private RecyclerListView cameraPhotoRecyclerView;
    private LinearLayoutManager cameraPhotoLayoutManager;
    private PhotoAttachAdapter cameraAttachAdapter;

    private ActionBarMenuItem dropDownContainer;
    public TextView dropDown;
    private Drawable dropDownDrawable;

    public RecyclerListView gridView;
    private GridLayoutManager layoutManager;
    private PhotoAttachAdapter adapter;
    private EmptyTextProgressView progressView;
    private RecyclerViewItemRangeSelector itemRangeSelector;
    private int gridExtraSpace;
    private boolean shouldSelect;
    private int alertOnlyOnce;
    private StoryPrivacyBottomSheet privacySheet;
    private ThanosEffect thanosEffect;

    private Drawable cameraDrawable;

    private boolean awaitingPlayer = false;
    private FlashViews flashViews;
    private boolean stoppingTakingVideo = false;

    private int currentSelectedCount;

    private boolean isHidden;
    private long botId;
    private String botLang;
    private boolean fromGallery;
    private File outputFile;

    ValueAnimator paddingAnimator;
    private int animateToPadding;
    private AnimatorSet pageAnimator;

    private BlurringShader.BlurManager blurManager;
    private CollageLayout lastCollageLayout;
    private FlashViews.ImageViewInvertable backButton;

    private SimpleTextView titleTextView;
    private LinearLayout actionBarButtons;
    private ToggleButton2 collageRemoveButton;
    private boolean isVideo = false;

    /* PAGE_COVER */
    private TimelineView coverTimelineView;
    private ButtonWithCounterView coverButton;

    /* PAGE_CAMERA */
    private ToggleButton2 flashButton;
    private CollageLayoutButton.CollageLayoutListView collageListView;
    private CollageLayoutView2 collageLayoutView;
    private CollageLayoutButton collageButton;
    private ImageView cameraViewThumb;
    private ToggleButton dualButton;
    private RecordControl recordControl;
    private VideoTimerView videoTimerView;
    private HintView2 cameraHint;
    private PhotoVideoSwitcherView modeSwitcherView;
    private HintTextView hintTextView;
    private HintTextView collageHintTextView;
    private DraftSavedHint draftSavedHint;
    private boolean wasGalleryOpen;
    private GalleryListView galleryListView;
    private boolean galleryClosing;
    private StoryThemeSheet themeSheet;

    /*PAGE_PREVIEW*/
    private CaptionStory captionEdit;
    private PreviewView previewView;
    private PreviewView.TextureViewHolder videoTextureHolder;
    private PreviewHighlightView previewHighlight;
    private TrashView trash;
    private VideoTimeView videoTimeView;
    private TimelineView timelineView;
    private FrameLayout videoTimelineContainerView;
    private PreviewButtons previewButtons;
    private DownloadButton downloadButton;
    private RoundVideoRecorder currentRoundRecorder;
    private HintView2 muteHint;
    private RLottieImageView muteButton;
    private PlayPauseButton playButton;
    private HintView2 dualHint;
    private HintView2 savedDualHint;
    private HintView2 removeCollageHint;
    private ImageView themeButton;
    private RLottieDrawable themeButtonDrawable;
    private RLottieDrawable muteButtonDrawable;

    /* EDIT_MODE_FILTER */
    private PhotoFilterView photoFilterView;
    private PhotoFilterView.EnhanceView photoFilterEnhanceView;
    private TextureView photoFilterViewTextureView;
    private PhotoFilterBlurControl photoFilterViewBlurControl;
    private PhotoFilterCurvesControl photoFilterViewCurvesControl;

    private boolean videoError;
    private long coverValue;

    /* EDIT_MODE_PAINT */
    private PaintView paintView;
    private RenderView paintViewRenderView;
    private View paintViewEntitiesView;
    private View paintViewRenderInputView;
    private View paintViewTextDim;
    private View paintViewSelectionContainerView;

    private StoryRecorder.Touchable previewTouchable;

    private StoryEntry outputEntry;

    private boolean showSavedDraftHint;

    private int underControls;
    private boolean underStatusBar;
    WindowManager windowManager;
    private final WindowManager.LayoutParams windowLayoutParams;
    private WindowView windowView;
    private FrameLayout actionBarContainer;
    private int insetLeft, insetTop, insetRight, insetBottom;
    private int flashButtonResId;
    private ContainerView containerView;

    public static final int PAGE_CAMERA = 0;
    public static final int PAGE_PREVIEW = 1;
    public static final int PAGE_COVER = 2;
    private int currentPage = PAGE_CAMERA;


    private FrameLayout previewContainer;
    private FrameLayout controlContainer;
    private FrameLayout navbarContainer;
    private FrameLayout captionContainer;
    private int previewW, previewH;
    private boolean scrollingY, scrollingX;
    private int shiftDp = -3;

    private View captionEditOverlay;

    private AnimatorSet cameraInitAnimation;
    protected DualCameraView cameraView;
    protected FrameLayout cameraIcon;
    protected PhotoAttachCameraCell cameraCell;
    private TextView recordTime;
    private boolean flashAnimationInProgress;
    private float[] cameraViewLocation = new float[2];
    private int[] viewPosition = new int[2];
    private float cameraViewOffsetX;
    private float cameraViewOffsetY;
    private float cameraViewOffsetBottomY;
    public boolean cameraOpened;
    private boolean canSaveCameraPreview;
    private boolean cameraAnimationInProgress;
    private float cameraOpenProgress;
    private int[] animateCameraValues = new int[5];
    private int videoRecordTime;
    private Runnable videoRecordRunnable;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);
    private FrameLayout cameraPanel;
    private ShutterButton shutterButton;
    private ZoomControlView zoomControlView;
    private AnimatorSet zoomControlAnimation;
    private Runnable zoomControlHideRunnable;
    private Runnable afterCameraInitRunnable;
    private Boolean isCameraFrontfaceBeforeEnteringEditMode = null;
    private TextView counterTextView;
    private TextView tooltipTextView;
    private ImageView switchCameraButton;
    private boolean takingPhoto;
    private boolean takingVideo = false;
    private static boolean mediaFromExternalCamera;
    private static ArrayList<Object> cameraPhotos = new ArrayList<>();
    public static HashMap<Object, Object> selectedPhotos = new HashMap<>();
    public static ArrayList<Object> selectedPhotosOrder = new ArrayList<>();
    public static int lastImageId = -1;
    private boolean cancelTakingPhotos;
    private boolean checkCameraWhenShown;

    private boolean mediaEnabled;
    private boolean videoEnabled;
    private boolean photoEnabled;
    private boolean documentsEnabled;

    private float pinchStartDistance;
    private float cameraZoom;
    private boolean zooming;
    private boolean zoomWas;
    private android.graphics.Rect hitRect = new Rect();

    private float lastY;
    private boolean pressed;
    private boolean maybeStartDraging;
    private boolean dragging;

    private boolean cameraPhotoRecyclerViewIgnoreLayout;

    private int itemSize = dp(80);
    private int lastItemSize = itemSize;
    private int itemsPerRow = 3;

    private boolean deviceHasGoodCamera;
    private boolean noCameraPermissions;
    private boolean noGalleryPermissions;
    private boolean requestingPermissions;

    private boolean ignoreLayout;
    private int lastNotifyWidth;

    private MediaController.AlbumEntry selectedAlbumEntry;
    private MediaController.AlbumEntry galleryAlbumEntry;
    private ArrayList<MediaController.AlbumEntry> dropDownAlbums;
    private float currentPanTranslationY;

    private boolean loading = true;

    public final static int group = 0;
    public final static int compress = 1;
    public final static int spoiler = 2;
    public final static int open_in = 3;
    public final static int preview_gap = 4;
    public final static int media_gap = 5;
    public final static int preview = 6;
    public final static int caption = 7;
    public final static int stars = 8;

    private ActionBarMenuSubItem spoilerItem;
    private ActionBarMenuSubItem compressItem;
    private ActionBarMenuSubItem starsItem;
    protected ActionBarMenuSubItem previewItem;
    public MessagePreviewView.ToggleButton captionItem;

    private Float frozenDismissProgress;
    private float fromRounding;
    private final RectF fromRect = new RectF();

    boolean forceDarkTheme;
    private AnimationNotificationsLocker notificationsLocker = new AnimationNotificationsLocker();
    private boolean showAvatarConstructor;

    public void updateAvatarPicker() {
        showAvatarConstructor = parentAlert.avatarPicker != 0 && !parentAlert.isPhotoPicker;
    }

    private class BasePhotoProvider extends PhotoViewer.EmptyPhotoViewerProvider {
        @Override
        public boolean isPhotoChecked(int index) {
            MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(index);
            return photoEntry != null && selectedPhotos.containsKey(photoEntry.imageId);
        }

        @Override
        public int setPhotoChecked(int index, VideoEditedInfo videoEditedInfo) {
            if (parentAlert.maxSelectedPhotos >= 0 && selectedPhotos.size() >= parentAlert.maxSelectedPhotos && !isPhotoChecked(index)) {
                return -1;
            }
            MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(index);
            if (photoEntry == null) {
                return -1;
            }
            if (checkSendMediaEnabled(photoEntry)) {
                return -1;
            }
            if (selectedPhotos.size() + 1 > maxCount()) {
                return -1;
            }
            boolean add = true;
            int num;
            if ((num = addToSelectedPhotos(photoEntry, -1)) == -1) {
                num = selectedPhotosOrder.indexOf(photoEntry.imageId);
            } else {
                add = false;
                photoEntry.editedInfo = null;
            }
            photoEntry.editedInfo = videoEditedInfo;

            int count = gridView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = gridView.getChildAt(a);
                if (view instanceof PhotoAttachPhotoCell) {
                    int tag = (Integer) view.getTag();
                    if (tag == index) {
                        if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                            ((PhotoAttachPhotoCell) view).setChecked(num, add, false);
                        } else {
                            ((PhotoAttachPhotoCell) view).setChecked(-1, add, false);
                        }
                        break;
                    }
                }
            }
            count = cameraPhotoRecyclerView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = cameraPhotoRecyclerView.getChildAt(a);
                if (view instanceof PhotoAttachPhotoCell) {
                    int tag = (Integer) view.getTag();
                    if (tag == index) {
                        if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                            ((PhotoAttachPhotoCell) view).setChecked(num, add, false);
                        } else {
                            ((PhotoAttachPhotoCell) view).setChecked(-1, add, false);
                        }
                        break;
                    }
                }
            }
            parentAlert.updateCountButton(add ? 1 : 2);
            return num;
        }

        @Override
        public int getSelectedCount() {
            return selectedPhotos.size();
        }

        @Override
        public ArrayList<Object> getSelectedPhotosOrder() {
            return selectedPhotosOrder;
        }

        @Override
        public HashMap<Object, Object> getSelectedPhotos() {
            return selectedPhotos;
        }

        @Override
        public int getPhotoIndex(int index) {
            MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(index);
            if (photoEntry == null) {
                return -1;
            }
            return selectedPhotosOrder.indexOf(photoEntry.imageId);
        }
    }

    private void setCurrentSpoilerVisible(int i, boolean visible) {
        PhotoViewer photoViewer = PhotoViewer.getInstance();
        int index = i == -1 ? photoViewer.getCurrentIndex() : i;
        List<Object> photos = photoViewer.getImagesArrLocals();
        boolean hasSpoiler = photos != null && !photos.isEmpty() && index < photos.size() && photos.get(index) instanceof MediaController.PhotoEntry && ((MediaController.PhotoEntry) photos.get(index)).hasSpoiler;

        if (hasSpoiler) {
            MediaController.PhotoEntry entry = (MediaController.PhotoEntry) photos.get(index);

            gridView.forAllChild(view -> {
                if (view instanceof PhotoAttachPhotoCell) {
                    PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                    if (cell.getPhotoEntry() == entry) {
                        cell.setHasSpoiler(visible, 250f);
                        cell.setStarsPrice(getStarsPrice(), selectedPhotos.size() > 1);
                    }
                }
            });
        }
    }

    public PhotoViewer.PhotoViewerProvider photoViewerProvider = new BasePhotoProvider() {
        @Override
        public void onOpen() {
            pauseCameraPreview();
            setCurrentSpoilerVisible(-1, true);
        }

        @Override
        public void onPreClose() {
            setCurrentSpoilerVisible(-1, false);
        }

        @Override
        public void onClose() {
            resumeCameraPreview();
            AndroidUtilities.runOnUIThread(()-> setCurrentSpoilerVisible(-1, true), 150);
        }

        @Override
        public void onEditModeChanged(boolean isEditMode) {
            onPhotoEditModeChanged(isEditMode);
        }

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
            PhotoAttachPhotoCell cell = getCellForIndex(index);
            if (cell != null) {
                int[] coords = new int[2];
                cell.getImageView().getLocationInWindow(coords);
                if (Build.VERSION.SDK_INT < 26) {
                    coords[0] -= parentAlert.getLeftInset();
                }
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1];
                object.parentView = gridView;
                object.imageReceiver = cell.getImageView().getImageReceiver();
                object.thumb = object.imageReceiver.getBitmapSafe();
                object.scale = cell.getScale();
                object.clipBottomAddition = (int) parentAlert.getClipLayoutBottom();
                cell.showCheck(false);
                return object;
            }

            return null;
        }

        @Override
        public void updatePhotoAtIndex(int index) {
            PhotoAttachPhotoCell cell = getCellForIndex(index);
            if (cell != null) {
                cell.getImageView().setOrientation(0, true);
                MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(index);
                if (photoEntry == null) {
                    return;
                }
                if (photoEntry.thumbPath != null) {
                    cell.getImageView().setImage(photoEntry.thumbPath, null, Theme.chat_attachEmptyDrawable);
                } else if (photoEntry.path != null) {
                    cell.getImageView().setOrientation(photoEntry.orientation, photoEntry.invert, true);
                    if (photoEntry.isVideo) {
                        cell.getImageView().setImage("vthumb://" + photoEntry.imageId + ":" + photoEntry.path, null, Theme.chat_attachEmptyDrawable);
                    } else {
                        cell.getImageView().setImage("thumb://" + photoEntry.imageId + ":" + photoEntry.path, null, Theme.chat_attachEmptyDrawable);
                    }
                } else {
                    cell.getImageView().setImageDrawable(Theme.chat_attachEmptyDrawable);
                }
            }
        }

        @Override
        public ImageReceiver.BitmapHolder getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
            PhotoAttachPhotoCell cell = getCellForIndex(index);
            if (cell != null) {
                return cell.getImageView().getImageReceiver().getBitmapSafe();
            }
            return null;
        }

        @Override
        public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
            PhotoAttachPhotoCell cell = getCellForIndex(index);
            if (cell != null) {
                cell.showCheck(true);
            }
        }

        @Override
        public void willHidePhotoViewer() {
            int count = gridView.getChildCount();
            for (int a = 0; a < count; a++) {
                View view = gridView.getChildAt(a);
                if (view instanceof PhotoAttachPhotoCell) {
                    PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                    cell.showCheck(true);
                }
            }
        }

        @Override
        public void onApplyCaption(CharSequence caption) {
            if (selectedPhotos.size() > 0 && selectedPhotosOrder.size() > 0) {
                Object o = selectedPhotos.get(selectedPhotosOrder.get(0));
                CharSequence firstPhotoCaption = null;
                ArrayList<TLRPC.MessageEntity> entities = null;
                if (o instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry1 = (MediaController.PhotoEntry) o;
                    firstPhotoCaption = photoEntry1.caption;
                    entities = photoEntry1.entities;
                }
                if (o instanceof MediaController.SearchImage) {
                    MediaController.SearchImage photoEntry1 = (MediaController.SearchImage) o;
                    firstPhotoCaption = photoEntry1.caption;
                    entities = photoEntry1.entities;
                }
                if (firstPhotoCaption != null) {
                    if (entities != null) {
                        if (!(firstPhotoCaption instanceof Spannable)) {
                            firstPhotoCaption = new SpannableStringBuilder(firstPhotoCaption);
                        }
                        MessageObject.addEntitiesToText(firstPhotoCaption, entities, false, false, false, false);
                    }
                }
                parentAlert.getCommentView().setText(AnimatedEmojiSpan.cloneSpans(firstPhotoCaption, AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW));
            }
        }

        @Override
        public boolean cancelButtonPressed() {
            return false;
        }

        @Override
        public void sendButtonPressed(int index, VideoEditedInfo videoEditedInfo, boolean notify, int scheduleDate, boolean forceDocument) {
            parentAlert.sent = true;
            MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(index);
            if (photoEntry != null) {
                photoEntry.editedInfo = videoEditedInfo;
            }
            if (selectedPhotos.isEmpty() && photoEntry != null) {
                addToSelectedPhotos(photoEntry, -1);
            }
            if (parentAlert.checkCaption(parentAlert.getCommentView().getText())) {
                return;
            }
            parentAlert.applyCaption();
            if (PhotoViewer.getInstance().hasCaptionForAllMedia) {
                HashMap<Object, Object> selectedPhotos = getSelectedPhotos();
                ArrayList<Object> selectedPhotosOrder = getSelectedPhotosOrder();
                if (!selectedPhotos.isEmpty()) {
                    for (int a = 0; a < selectedPhotosOrder.size(); a++) {
                        Object o = selectedPhotos.get(selectedPhotosOrder.get(a));
                        if (o instanceof MediaController.PhotoEntry) {
                            MediaController.PhotoEntry photoEntry1 = (MediaController.PhotoEntry) o;
                            if (a == 0) {
                                CharSequence[] caption = new CharSequence[]{PhotoViewer.getInstance().captionForAllMedia};
                                photoEntry1.entities = MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(caption, false);
                                photoEntry1.caption = caption[0];
                                if (parentAlert.checkCaption(photoEntry1.caption)) {
                                    return;
                                }
                            } else {
                                photoEntry1.caption = null;
                            }
                        }
                    }
                }
            }
            parentAlert.delegate.didPressedButton(7, true, notify, scheduleDate, 0, parentAlert.isCaptionAbove(), forceDocument);
            selectedPhotos.clear();
            cameraPhotos.clear();
            selectedPhotosOrder.clear();
            selectedPhotos.clear();
        }

        @Override
        public boolean allowCaption() {
            return !parentAlert.isPhotoPicker;
        }

        @Override
        public long getDialogId() {
            if (parentAlert.baseFragment instanceof ChatActivity)
                return ((ChatActivity) parentAlert.baseFragment).getDialogId();
            return super.getDialogId();
        }

        @Override
        public boolean canMoveCaptionAbove() {
            return parentAlert != null && parentAlert.baseFragment instanceof ChatActivity;
        }
        @Override
        public boolean isCaptionAbove() {
            return parentAlert != null && parentAlert.captionAbove;
        }
        @Override
        public void moveCaptionAbove(boolean above) {
            if (parentAlert == null || parentAlert.captionAbove == above) return;
            parentAlert.setCaptionAbove(above);
            captionItem.setState(!parentAlert.captionAbove, true);
        }
    };

    protected void updateCheckedPhotoIndices() {
        if (!(parentAlert.baseFragment instanceof ChatActivity)) {
            return;
        }
        int count = gridView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = gridView.getChildAt(a);
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition((Integer) cell.getTag());
                if (photoEntry != null) {
                    cell.setNum(selectedPhotosOrder.indexOf(photoEntry.imageId));
                }
            }
        }
        count = cameraPhotoRecyclerView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = cameraPhotoRecyclerView.getChildAt(a);
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition((Integer) cell.getTag());
                if (photoEntry != null) {
                    cell.setNum(selectedPhotosOrder.indexOf(photoEntry.imageId));
                }
            }
        }
    }

    protected void updateCheckedPhotos() {
        if (!(parentAlert.baseFragment instanceof ChatActivity)) {
            return;
        }
        int count = gridView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = gridView.getChildAt(a);
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                int position = gridView.getChildAdapterPosition(view);
                if (adapter.needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                    position--;
                }
                MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(position);
                cell.setHasSpoiler(photoEntry != null && photoEntry.hasSpoiler);
                if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                    cell.setChecked(photoEntry != null ? selectedPhotosOrder.indexOf(photoEntry.imageId) : -1, photoEntry != null && selectedPhotos.containsKey(photoEntry.imageId), true);
                } else {
                    cell.setChecked(-1, photoEntry != null && selectedPhotos.containsKey(photoEntry.imageId), true);
                }
            }
        }
        count = cameraPhotoRecyclerView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = cameraPhotoRecyclerView.getChildAt(a);
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                int position = cameraPhotoRecyclerView.getChildAdapterPosition(view);
                if (adapter.needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                    position--;
                }
                MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(position);
                cell.setHasSpoiler(photoEntry != null && photoEntry.hasSpoiler);
                if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                    cell.setChecked(photoEntry != null ? selectedPhotosOrder.indexOf(photoEntry.imageId) : -1, photoEntry != null && selectedPhotos.containsKey(photoEntry.imageId), true);
                } else {
                    cell.setChecked(-1, photoEntry != null && selectedPhotos.containsKey(photoEntry.imageId), true);
                }
            }
        }
    }

    private MediaController.PhotoEntry getPhotoEntryAtPosition(int position) {
        if (position < 0) {
            return null;
        }
        int cameraCount = cameraPhotos.size();
        if (position < cameraCount) {
            return (MediaController.PhotoEntry) cameraPhotos.get(position);
        }
        position -= cameraCount;
        if (selectedAlbumEntry != null && position < selectedAlbumEntry.photos.size()) {
            return selectedAlbumEntry.photos.get(position);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected ArrayList<Object> getAllPhotosArray() {
        ArrayList<Object> arrayList;
        if (selectedAlbumEntry != null) {
            if (!cameraPhotos.isEmpty()) {
                arrayList = new ArrayList<>(selectedAlbumEntry.photos.size() + cameraPhotos.size());
                arrayList.addAll(cameraPhotos);
                arrayList.addAll(selectedAlbumEntry.photos);
            } else {
                arrayList = (ArrayList) selectedAlbumEntry.photos;
            }
        } else if (!cameraPhotos.isEmpty()) {
            arrayList = cameraPhotos;
        } else {
            arrayList = new ArrayList<>(0);
        }
        return arrayList;
    }

    public ChatAttachAlertPhotoLayout(ChatAttachAlert alert, Context context, boolean forceDarkTheme, boolean needCamera, Theme.ResourcesProvider resourcesProvider) {
        super(alert, context, resourcesProvider);

        this.forceDarkTheme = forceDarkTheme;
        this.needCamera = needCamera;
        windowLayoutParams = new WindowManager.LayoutParams();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.albumsDidLoad);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.cameraInitied);
        FrameLayout container = alert.getContainer();
        showAvatarConstructor = parentAlert.avatarPicker != 0;

        cameraDrawable = context.getResources().getDrawable(R.drawable.instant_camera).mutate();

        ActionBarMenu menu = parentAlert.actionBar.createMenu();
        dropDownContainer = new ActionBarMenuItem(context, menu, 0, 0, resourcesProvider) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                info.setText(dropDown.getText());
            }
        };
        dropDownContainer.setSubMenuOpenSide(1);
        parentAlert.actionBar.addView(dropDownContainer, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, AndroidUtilities.isTablet() ? 64 : 56, 0, 40, 0));
        dropDownContainer.setOnClickListener(view -> dropDownContainer.toggleSubMenu());

        dropDown = new TextView(context);
        dropDown.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        dropDown.setGravity(Gravity.LEFT);
        dropDown.setSingleLine(true);
        dropDown.setLines(1);
        dropDown.setMaxLines(1);
        dropDown.setEllipsize(TextUtils.TruncateAt.END);
        dropDown.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        dropDown.setText(LocaleController.getString(R.string.ChatGallery));
        dropDown.setTypeface(AndroidUtilities.bold());
        dropDownDrawable = context.getResources().getDrawable(R.drawable.ic_arrow_drop_down).mutate();
        dropDownDrawable.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
        dropDown.setCompoundDrawablePadding(dp(4));
        dropDown.setPadding(0, 0, dp(10), 0);
        dropDownContainer.addView(dropDown, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        checkCamera(false);

        captionItem = new MessagePreviewView.ToggleButton(
            context,
            R.raw.position_below, getString(R.string.CaptionAbove),
            R.raw.position_above, getString(R.string.CaptionBelow),
            resourcesProvider
        );
        captionItem.setState(!parentAlert.captionAbove, false);

        previewItem = parentAlert.selectedMenuItem.addSubItem(preview, R.drawable.msg_view_file, LocaleController.getString(R.string.AttachMediaPreviewButton));

        parentAlert.selectedMenuItem.addColoredGap(preview_gap);
        parentAlert.selectedMenuItem.addSubItem(open_in, R.drawable.msg_openin, LocaleController.getString(R.string.OpenInExternalApp));
        compressItem = parentAlert.selectedMenuItem.addSubItem(compress, R.drawable.msg_filehq, LocaleController.getString(R.string.SendWithoutCompression));
        parentAlert.selectedMenuItem.addSubItem(group, R.drawable.msg_ungroup, LocaleController.getString(R.string.SendWithoutGrouping));
        parentAlert.selectedMenuItem.addColoredGap(media_gap);
        spoilerItem = parentAlert.selectedMenuItem.addSubItem(spoiler, R.drawable.msg_spoiler, LocaleController.getString(R.string.EnablePhotoSpoiler));
        parentAlert.selectedMenuItem.addSubItem(caption, captionItem);
        starsItem = parentAlert.selectedMenuItem.addSubItem(stars, R.drawable.menu_feature_paid, getString(R.string.PaidMediaButton));
        parentAlert.selectedMenuItem.setFitSubItems(true);

        gridView = new RecyclerListView(context, resourcesProvider) {
            @Override
            public boolean onTouchEvent(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN && e.getY() < parentAlert.scrollOffsetY[0] - dp(80)) {
                    return false;
                }
                return super.onTouchEvent(e);
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN && e.getY() < parentAlert.scrollOffsetY[0] - dp(80)) {
                    return false;
                }
                return super.onInterceptTouchEvent(e);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                PhotoViewer.getInstance().checkCurrentImageVisibility();
            }
        };
        gridView.setFastScrollEnabled(RecyclerListView.FastScroll.DATE_TYPE);
        gridView.setFastScrollVisible(true);
        gridView.getFastScroll().setAlpha(0f);
        gridView.getFastScroll().usePadding = false;
        gridView.setAdapter(adapter = new PhotoAttachAdapter(context, needCamera));
        adapter.createCache();
        gridView.setClipToPadding(false);
        gridView.setItemAnimator(null);
        gridView.setLayoutAnimation(null);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setGlowColor(getThemedColor(Theme.key_dialogScrollGlow));
        addView(gridView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        gridView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            boolean parentPinnedToTop;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (gridView.getChildCount() <= 0) {
                    return;
                }
                parentAlert.updateLayout(ChatAttachAlertPhotoLayout.this, true, dy);
                if (adapter.getTotalItemsCount() > SHOW_FAST_SCROLL_MIN_COUNT) {
                    if (parentPinnedToTop != parentAlert.pinnedToTop) {
                        parentPinnedToTop = parentAlert.pinnedToTop;
                        gridView.getFastScroll().animate().alpha(parentPinnedToTop ? 1f : 0f).setDuration(100).start();
                    }
                } else {
                    gridView.getFastScroll().setAlpha(0);
                }
                if (dy != 0) {
                    checkCameraViewPosition();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int offset = dp(13) + (parentAlert.selectedMenuItem != null ? dp(parentAlert.selectedMenuItem.getAlpha() * 26) : 0);
                    int backgroundPaddingTop = parentAlert.getBackgroundPaddingTop();
                    int top = parentAlert.scrollOffsetY[0] - backgroundPaddingTop - offset;
                    if (top + backgroundPaddingTop < ActionBar.getCurrentActionBarHeight() + parentAlert.topCommentContainer.getMeasuredHeight() * parentAlert.topCommentContainer.getAlpha()) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) gridView.findViewHolderForAdapterPosition(0);
                        if (holder != null && holder.itemView.getTop() > dp(7)) {
                            gridView.smoothScrollBy(0, holder.itemView.getTop() - dp(7));
                        }
                    }
                }
            }
        });
        layoutManager = new GridLayoutManager(context, itemSize) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public int calculateDyToMakeVisible(View view, int snapPreference) {
                        int dy = super.calculateDyToMakeVisible(view, snapPreference);
                        dy -= (gridView.getPaddingTop() - dp(7));
                        return dy;
                    }

                    @Override
                    protected int calculateTimeForDeceleration(int dx) {
                        return super.calculateTimeForDeceleration(dx) * 2;
                    }
                };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        };
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == adapter.itemsCount - 1) {
                    return layoutManager.getSpanCount();
                }
                return itemSize + (position % itemsPerRow != itemsPerRow - 1 ? dp(5) : 0);
            }
        });
        gridView.setLayoutManager(layoutManager);
        gridView.setOnItemClickListener((view, position, x, y) -> {
            if (!mediaEnabled || parentAlert.destroyed) {
                return;
            }
            BaseFragment fragment = parentAlert.baseFragment;
            if (fragment == null) {
                fragment = LaunchActivity.getLastFragment();
            }
            if (fragment == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (adapter.needCamera && selectedAlbumEntry == galleryAlbumEntry && position == 0 && noCameraPermissions) {
                    try {
                        fragment.getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 18);
                    } catch (Exception ignore) {

                    }
                    return;
                } else if (noGalleryPermissions) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        try {
                            fragment.getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                        } catch (Exception ignore) {}
                    } else {
                        try {
                            fragment.getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
                        } catch (Exception ignore) {}
                    }
                    return;
                }
            }
            if (position != 0 || !needCamera || selectedAlbumEntry != galleryAlbumEntry) {
                if (selectedAlbumEntry == galleryAlbumEntry && needCamera) {
                    position--;
                }
                if (showAvatarConstructor) {
                    if (position == 0) {
                        if (!(view instanceof AvatarConstructorPreviewCell)) {
                            return;
                        }
                        showAvatarConstructorFragment((AvatarConstructorPreviewCell) view, null);
                        parentAlert.dismiss();
                    }
                    position--;
                }
                ArrayList<Object> arrayList = getAllPhotosArray();
                if (position < 0 || position >= arrayList.size()) {
                    return;
                }
                if (parentAlert.delegate != null && parentAlert.delegate.selectItemOnClicking() && arrayList.get(position) instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) arrayList.get(position);
                    selectedPhotos.clear();
                    if (photoEntry != null) {
                        addToSelectedPhotos(photoEntry, -1);
                    }
                    parentAlert.applyCaption();
                    parentAlert.delegate.didPressedButton(7, true, true, 0, 0, parentAlert.isCaptionAbove(), false);
                    selectedPhotos.clear();
                    cameraPhotos.clear();
                    selectedPhotosOrder.clear();
                    selectedPhotos.clear();
                    return;
                }
                PhotoViewer.getInstance().setParentActivity(fragment, resourcesProvider);
                PhotoViewer.getInstance().setParentAlert(parentAlert);
                PhotoViewer.getInstance().setMaxSelectedPhotos(parentAlert.maxSelectedPhotos, parentAlert.allowOrder);
                ChatActivity chatActivity;
                int type;
                if (parentAlert.isPhotoPicker && parentAlert.isStickerMode) {
                    type = PhotoViewer.SELECT_TYPE_STICKER;
                    if (parentAlert.baseFragment instanceof ChatActivity) {
                        chatActivity = (ChatActivity) parentAlert.baseFragment;
                    } else {
                        chatActivity = null;
                    }
                } else if (parentAlert.avatarPicker != 0) {
                    chatActivity = null;
                    type = PhotoViewer.SELECT_TYPE_AVATAR;
                } else if (parentAlert.baseFragment instanceof ChatActivity) {
                    chatActivity = (ChatActivity) parentAlert.baseFragment;
                    type = 0;
                } else if (parentAlert.allowEnterCaption) {
                    chatActivity = null;
                    type = 0;
                } else {
                    chatActivity = null;
                    type = 4;
                }
                if (!parentAlert.delegate.needEnterComment()) {
                    AndroidUtilities.hideKeyboard(fragment.getFragmentView().findFocus());
                    AndroidUtilities.hideKeyboard(parentAlert.getContainer().findFocus());
                }
                if (selectedPhotos.size() > 0 && selectedPhotosOrder.size() > 0) {
                    Object o = selectedPhotos.get(selectedPhotosOrder.get(0));
                    if (o instanceof MediaController.PhotoEntry) {
                        MediaController.PhotoEntry photoEntry1 = (MediaController.PhotoEntry) o;
                        photoEntry1.caption = parentAlert.getCommentView().getText();
                    }
                    if (o instanceof MediaController.SearchImage) {
                        MediaController.SearchImage photoEntry1 = (MediaController.SearchImage) o;
                        photoEntry1.caption = parentAlert.getCommentView().getText();
                    }
                }
                if (parentAlert.getAvatarFor() != null) {
                    boolean isVideo = false;
                    if (arrayList.get(position) instanceof MediaController.PhotoEntry) {
                        isVideo = ((MediaController.PhotoEntry) arrayList.get(position)).isVideo;
                    }
                    parentAlert.getAvatarFor().isVideo = isVideo;
                }

                boolean hasSpoiler = arrayList.get(position) instanceof MediaController.PhotoEntry && ((MediaController.PhotoEntry) arrayList.get(position)).hasSpoiler;
                Object object = arrayList.get(position);
                if (object instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) object;
                    if (checkSendMediaEnabled(photoEntry)) {
                        return;
                    }
                }
                if (hasSpoiler) {
                    setCurrentSpoilerVisible(position, false);
                }
                int finalPosition = position;
                BaseFragment finalFragment = fragment;
                AndroidUtilities.runOnUIThread(() -> {
                    int avatarType = type;
                    if (parentAlert.isPhotoPicker && !parentAlert.isStickerMode) {
                        PhotoViewer.getInstance().setParentActivity(finalFragment);
                        PhotoViewer.getInstance().setMaxSelectedPhotos(0, false);
                        avatarType = PhotoViewer.SELECT_TYPE_WALLPAPER;
                    }
                    PhotoViewer.getInstance().openPhotoForSelect(arrayList, finalPosition, avatarType, false, photoViewerProvider, chatActivity);
                    PhotoViewer.getInstance().setAvatarFor(parentAlert.getAvatarFor());
                    if (parentAlert.isPhotoPicker && !parentAlert.isStickerMode) {
                        PhotoViewer.getInstance().closePhotoAfterSelect = false;
                    }
                    if (parentAlert.isStickerMode) {
                        PhotoViewer.getInstance().enableStickerMode(null, false, parentAlert.customStickerHandler);
                    }
                    if (captionForAllMedia()) {
                        PhotoViewer.getInstance().setCaption(parentAlert.getCommentView().getText());
                    }
                }, hasSpoiler ? 250 : 0);
            } else {
                if (SharedConfig.inappCamera) {
                    openCamera(true);
                } else {
                    if (parentAlert.delegate != null) {
                        parentAlert.delegate.didPressedButton(0, false, true, 0, 0, parentAlert.isCaptionAbove(), false);
                    }
                }
            }
        });
        gridView.setOnItemLongClickListener((view, position) -> {
            if (parentAlert.storyMediaPicker) {
                return false;
            }
            if (position == 0 && selectedAlbumEntry == galleryAlbumEntry) {
                if (parentAlert.delegate != null) {
                    parentAlert.delegate.didPressedButton(0, false, true, 0, 0, parentAlert.isCaptionAbove(), false);
                }
                return true;
            } else if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                itemRangeSelector.setIsActive(view, true, position, shouldSelect = !cell.isChecked());
            }
            return false;
        });
        itemRangeSelector = new RecyclerViewItemRangeSelector(new RecyclerViewItemRangeSelector.RecyclerViewItemRangeSelectorDelegate() {
            @Override
            public int getItemCount() {
                return adapter.getItemCount();
            }

            @Override
            public void setSelected(View view, int index, boolean selected) {
                if (selected != shouldSelect || !(view instanceof PhotoAttachPhotoCell)) {
                    return;
                }
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                cell.callDelegate();
            }

            @Override
            public boolean isSelected(int index) {
                MediaController.PhotoEntry entry = adapter.getPhoto(index);
                return entry != null && selectedPhotos.containsKey(entry.imageId);
            }

            @Override
            public boolean isIndexSelectable(int index) {
                return adapter.getItemViewType(index) == 0;
            }

            @Override
            public void onStartStopSelection(boolean start) {
                alertOnlyOnce = start ? 1 : 0;
                gridView.hideSelector(true);
            }
        });
        gridView.addOnItemTouchListener(itemRangeSelector);

        progressView = new EmptyTextProgressView(context, null, resourcesProvider);
        progressView.setText(LocaleController.getString(R.string.NoPhotos));
        progressView.setOnTouchListener(null);
        progressView.setTextSize(16);
        addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (loading) {
            progressView.showProgress();
        } else {
            progressView.showTextView();
        }

        Paint recordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        recordPaint.setColor(0xffda564d);
        recordTime = new TextView(context) {

            float alpha = 0f;
            boolean isIncr;

            @Override
            protected void onDraw(Canvas canvas) {

                recordPaint.setAlpha((int) (125 + 130 * alpha));

                if (!isIncr) {
                    alpha -= 16 / 600.0f;
                    if (alpha <= 0) {
                        alpha = 0;
                        isIncr = true;
                    }
                } else {
                    alpha += 16 / 600.0f;
                    if (alpha >= 1) {
                        alpha = 1;
                        isIncr = false;
                    }
                }
                super.onDraw(canvas);
                canvas.drawCircle(dp(14), getMeasuredHeight() / 2, dp(4), recordPaint);
                invalidate();
            }
        };
        AndroidUtilities.updateViewVisibilityAnimated(recordTime, false, 1f, false);
        recordTime.setBackgroundResource(R.drawable.system);
        recordTime.getBackground().setColorFilter(new PorterDuffColorFilter(0x66000000, PorterDuff.Mode.MULTIPLY));
        recordTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        recordTime.setTypeface(AndroidUtilities.bold());
        recordTime.setAlpha(0.0f);
        recordTime.setTextColor(0xffffffff);
        recordTime.setPadding(dp(24), dp(5), dp(10), dp(5));
        container.addView(recordTime, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 16, 0, 0));

        cameraPanel = new FrameLayout(context) {
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                // Convert dp to pixels for margins
                int topMargin = (int) (48 * getContext().getResources().getDisplayMetrics().density);
                int bottomMargin = (int) (90 * getContext().getResources().getDisplayMetrics().density);

                // Adjust layout boundaries
                final int insetTopAdjusted = underStatusBar ? insetTop + topMargin : topMargin;
                final int insetBottomAdjusted = bottom - top - bottomMargin;

                final int w = right - left;
                final int h = insetBottomAdjusted - insetTopAdjusted;

                // Position child views within the adjusted bounds
                actionBarContainer.layout(0, insetTopAdjusted, previewW, insetTopAdjusted + actionBarContainer.getMeasuredHeight());
//                actionBarContainer.layout(0, 0, previewW, actionBarContainer.getMeasuredHeight());
                controlContainer.layout(
                        0,
                        insetBottomAdjusted - controlContainer.getMeasuredHeight(),
                        previewW,
                        insetBottomAdjusted
                );
                navbarContainer.layout(
                        0,
                        insetBottomAdjusted,
                        previewW,
                        insetBottomAdjusted + navbarContainer.getMeasuredHeight()
                );
                flashViews.foregroundView.layout(0, insetTopAdjusted, w, insetTopAdjusted + h);
            }
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                final int W = MeasureSpec.getSize(widthMeasureSpec);
                final int H = MeasureSpec.getSize(heightMeasureSpec);
                measureChildExactly(actionBarContainer, previewW, dp(56 + 56 + 38));
                measureChildExactly(controlContainer, previewW, dp(220));
                measureChildExactly(navbarContainer, previewW, underControls);
                measureChildExactly(flashViews.foregroundView, W, H);
                for (int i = 0; i < getChildCount(); ++i) {
                    View child = getChildAt(i);
                    if (child instanceof ItemOptions.DimView) {
                        measureChildExactly(child, W, H);
                    }
                }

                setMeasuredDimension(W, H);
            }

            private void measureChildExactly(View child, int width, int height) {
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        };
        cameraPanel.setVisibility(View.GONE);
        cameraPanel.setAlpha(0.0f);
        initViews();
        windowView.setVisibility(View.VISIBLE);
        windowView.setAlpha(1.0f);
//        container.addView(windowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 0, 0));
        counterTextView = new TextView(context);
        counterTextView.setBackgroundResource(R.drawable.photos_rounded);
        counterTextView.setVisibility(View.GONE);
        counterTextView.setTextColor(0xffffffff);
        counterTextView.setGravity(Gravity.CENTER);
        counterTextView.setPivotX(0);
        counterTextView.setPivotY(0);
        counterTextView.setTypeface(AndroidUtilities.bold());
        counterTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.photos_arrow, 0);
        counterTextView.setCompoundDrawablePadding(dp(4));
        counterTextView.setPadding(dp(16), 0, dp(16), 0);
        container.addView(counterTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 38, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 100 + 16));
        counterTextView.setOnClickListener(v -> {
            if (cameraView == null) {
                return;
            }
            openPhotoViewer(null, false, false);
            CameraController.getInstance().stopPreview(cameraView.getCameraSessionObject());
        });

        zoomControlView = new ZoomControlView(context);
        zoomControlView.setVisibility(View.GONE);
        zoomControlView.setAlpha(0.0f);
        container.addView(zoomControlView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 50, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 100 + 16));
        zoomControlView.setDelegate(zoom -> {
            if (cameraView != null) {
                cameraView.setZoom(cameraZoom = zoom);
            }
            showZoomControls(true, true);
        });

        tooltipTextView = new TextView(context);
        tooltipTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tooltipTextView.setTextColor(0xffffffff);
        tooltipTextView.setText(LocaleController.getString(R.string.TapForVideo));
        tooltipTextView.setShadowLayer(dp(3.33333f), 0, dp(0.666f), 0x4c000000);
        tooltipTextView.setPadding(dp(6), 0, dp(6), 0);
        cameraPanel.addView(tooltipTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 16));

        cameraPhotoRecyclerView = new RecyclerListView(context, resourcesProvider) {
            @Override
            public void requestLayout() {
                if (cameraPhotoRecyclerViewIgnoreLayout) {
                    return;
                }
                super.requestLayout();
            }
        };
        cameraPhotoRecyclerView.setVerticalScrollBarEnabled(true);
        cameraPhotoRecyclerView.setAdapter(cameraAttachAdapter = new PhotoAttachAdapter(context, false));
        cameraAttachAdapter.createCache();
        cameraPhotoRecyclerView.setClipToPadding(false);
        cameraPhotoRecyclerView.setPadding(dp(8), 0, dp(8), 0);
        cameraPhotoRecyclerView.setItemAnimator(null);
        cameraPhotoRecyclerView.setLayoutAnimation(null);
        cameraPhotoRecyclerView.setOverScrollMode(RecyclerListView.OVER_SCROLL_NEVER);
        cameraPhotoRecyclerView.setVisibility(View.INVISIBLE);
        cameraPhotoRecyclerView.setAlpha(0.0f);
        container.addView(cameraPhotoRecyclerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 80));
        cameraPhotoLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        cameraPhotoRecyclerView.setLayoutManager(cameraPhotoLayoutManager);
        cameraPhotoRecyclerView.setOnItemClickListener((view, position) -> {
            if (view instanceof PhotoAttachPhotoCell) {
                ((PhotoAttachPhotoCell) view).callDelegate();
            }
        });
    }


    public static final int EDIT_MODE_NONE = -1;
    public static final int EDIT_MODE_PAINT = 0;
    public static final int EDIT_MODE_FILTER = 1;
    public static final int EDIT_MODE_TIMELINE = 2;
    private int currentEditMode = EDIT_MODE_NONE;

    private int openType = 0;
    private float dismissProgress;
    private float openProgress;

    private class ContainerView extends FrameLayout {
        public ContainerView(Context context) {
            super(context);
        }

        public void updateBackground() {
            if (openType == 0) {
                setBackground(Theme.createRoundRectDrawable(dp(12), 0xff000000));
            } else {
                setBackground(null);
            }
        }

        @Override
        public void invalidate() {
            if (cameraAnimationInProgress) {
                return;
            }
            super.invalidate();
        }

        private static final int CAMERA_PANEL_TOP_MARGIN_DP = 48;
        private static final int CAMERA_PANEL_BOTTOM_MARGIN_DP = 100;

        // Convert dp to pixels
        private final int cameraPanelTopMargin = dp(CAMERA_PANEL_TOP_MARGIN_DP);
        private final int cameraPanelBottomMargin = dp(CAMERA_PANEL_BOTTOM_MARGIN_DP);

        private float translationY1;
        private float translationY2;

        public void setTranslationY2(float translationY2) {
            super.setTranslationY(this.translationY1 + (this.translationY2 = translationY2));
        }

        public float getTranslationY1() {
            return translationY1;
        }

        public float getTranslationY2() {
            return translationY2;
        }

        @Override
        public void setTranslationY(float translationY) {
            super.setTranslationY((this.translationY1 = translationY) + translationY2);

            dismissProgress = Utilities.clamp(translationY / getMeasuredHeight() * 4, 1, 0);
            checkBackgroundVisibility();
            windowView.invalidate();

            final float scale = 1f - .1f * Utilities.clamp(getTranslationY() / AndroidUtilities.dp(320), 1, 0);
            setScaleX(scale);
            setScaleY(scale);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int t = underStatusBar ? insetTop : 0;

            final int w = right - left;
            final int h = bottom - top;

            previewContainer.layout(0, 0, previewW, previewH);
            previewContainer.setPivotX(previewW * .5f);
            cameraPanel.layout(0, 0, previewW, previewH);
            captionContainer.layout(0, 0, previewW, previewH);
            if (captionEditOverlay != null) {
                captionEditOverlay.layout(0, 0, w, h);
            }

            if (captionEdit.mentionContainer != null) {
                captionEdit.mentionContainer.layout(0, 0, previewW, previewH);
                captionEdit.updateMentionsLayoutPosition();
            }

            if (photoFilterView != null) {
                photoFilterView.layout(0, 0, photoFilterView.getMeasuredWidth(), photoFilterView.getMeasuredHeight());
            }
            if (paintView != null) {
                paintView.layout(0, 0, paintView.getMeasuredWidth(), paintView.getMeasuredHeight());
            }

            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child instanceof ItemOptions.DimView) {
                    child.layout(0, 0, w, h);
                }
            }

            setPivotX((right - left) / 2f);
            setPivotY(-h * .2f);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int W = MeasureSpec.getSize(widthMeasureSpec);
            final int H = MeasureSpec.getSize(heightMeasureSpec);

            measureChildExactly(previewContainer, previewW, previewH);
            applyFilterMatrix();
            measureChildExactly(cameraPanel, previewW, previewH);
            measureChildExactly(captionContainer, previewW, previewH);
            if (captionEditOverlay != null) {
                measureChildExactly(captionEditOverlay, W, H);
            }

            if (captionEdit.mentionContainer != null) {
                measureChildExactly(captionEdit.mentionContainer, previewW, previewH);
            }

            if (photoFilterView != null) {
                measureChildExactly(photoFilterView, W, H);
            }
            if (paintView != null) {
                measureChildExactly(paintView, W, H);
            }

            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child instanceof ItemOptions.DimView) {
                    measureChildExactly(child, W, H);
                }
            }

            setMeasuredDimension(W, H);
        }

        private void measureChildExactly(View child, int width, int height) {
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        private final Paint topGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private LinearGradient topGradient;

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean r = super.drawChild(canvas, child, drawingTime);
            if (child == previewContainer) {
                final float top = underStatusBar ? AndroidUtilities.statusBarHeight : 0;
                if (topGradient == null) {
                    topGradient = new LinearGradient(0, top, 0, top + dp(72), new int[] {0x40000000, 0x00000000}, new float[] { top / (top + dp(72)), 1 }, Shader.TileMode.CLAMP );
                    topGradientPaint.setShader(topGradient);
                }
                topGradientPaint.setAlpha(0xFF);
                AndroidUtilities.rectTmp.set(0, 0, getWidth(), dp(72 + 12) + top);
                canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(12), dp(12), topGradientPaint);
            }
            return r;
        }
    }

    private void createFilterPhotoView() {
        if (photoFilterView != null || outputEntry == null) {
            return;
        }

        Bitmap photoBitmap = null;
        if (!outputEntry.isVideo) {
            if (outputEntry.filterFile == null) {
                photoBitmap = previewView.getPhotoBitmap();
            } else {
                photoBitmap = StoryEntry.getScaledBitmap(opts -> BitmapFactory.decodeFile(outputEntry.file.getAbsolutePath(), opts), AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y, true, true);
            }
        }
        if (photoBitmap == null && !outputEntry.isVideo) {
            return;
        }

        photoFilterView = new PhotoFilterView(parentAlert.baseFragment.getContext(), previewView.getTextureView(), photoBitmap, previewView.getOrientation(), outputEntry == null ? null : outputEntry.filterState, null, 0, false, false, blurManager, resourcesProvider);
        containerView.addView(photoFilterView);
        if (photoFilterEnhanceView != null) {
            photoFilterEnhanceView.setFilterView(photoFilterView);
        }
        photoFilterViewTextureView = photoFilterView.getMyTextureView();
        if (photoFilterViewTextureView != null) {
            photoFilterViewTextureView.setOpaque(false);
        }
        previewView.setFilterTextureView(photoFilterViewTextureView, photoFilterView);
        if (photoFilterViewTextureView != null) {
            photoFilterViewTextureView.setAlpha(0f);
            photoFilterViewTextureView.animate().alpha(1f).setDuration(220).start();
        }
        applyFilterMatrix();
        photoFilterViewBlurControl = photoFilterView.getBlurControl();
        if (photoFilterViewBlurControl != null) {
            previewContainer.addView(photoFilterViewBlurControl);
        }
        photoFilterViewCurvesControl = photoFilterView.getCurveControl();
        if (photoFilterViewCurvesControl != null) {
            previewContainer.addView(photoFilterViewCurvesControl);
        }
        orderPreviewViews();

        photoFilterView.getDoneTextView().setOnClickListener(v -> {
            switchToEditMode(EDIT_MODE_NONE, true);
        });
        photoFilterView.getCancelTextView().setOnClickListener(v -> {
            switchToEditMode(EDIT_MODE_NONE, true);
        });
        photoFilterView.getToolsView().setVisibility(View.GONE);
        photoFilterView.getToolsView().setAlpha(0f);
        photoFilterView.getToolsView().setTranslationY(AndroidUtilities.dp(186));
        photoFilterView.init();
    }

    public boolean onBackPressedChecked() {
        if (cameraAnimationInProgress) {
            return false;
        }
        if (captionEdit != null && captionEdit.stopRecording()) {
            return false;
        }
        if (takingVideo) {
            recordControl.stopRecording();
            return false;
        }
        if (takingPhoto) {
            return false;
        }
        if (captionEdit.onBackPressed()) {
            return false;
        } else if (themeSheet != null) {
            themeSheet.dismiss();
            return false;
        } else if (galleryListView != null) {
            if (galleryListView.onBackPressed()) {
                return false;
            }
            animateGalleryListView(false);
            lastGallerySelectedAlbum = null;
            return false;
        } else if (currentEditMode == EDIT_MODE_PAINT && paintView != null && paintView.onBackPressed()) {
            return false;
        } else if (currentEditMode > EDIT_MODE_NONE) {
            switchToEditMode(EDIT_MODE_NONE, true);
            return false;
        } else if (currentPage == PAGE_CAMERA && collageLayoutView.hasContent()) {
            collageLayoutView.clear(true);
            updateActionBarButtons(true);
            return false;
        } else if (currentPage == PAGE_PREVIEW && (outputEntry == null || !outputEntry.isRepost && !outputEntry.isRepostMessage) && (outputEntry == null || !outputEntry.isEdit || (paintView != null && paintView.hasChanges()) || outputEntry.editedMedia || outputEntry.editedCaption)) {
            if (paintView != null && paintView.onBackPressed()) {
                return false;
            } else if (botId == 0 && (fromGallery && !collageLayoutView.hasLayout() && (paintView == null || !paintView.hasChanges()) && (outputEntry == null || outputEntry.filterFile == null) || !previewButtons.isShareEnabled()) && (outputEntry == null || !outputEntry.isEdit || !outputEntry.isRepost && !outputEntry.isRepostMessage)) {
                navigateTo(PAGE_CAMERA, true);
            } else {
                if (botId != 0) {
                    close(true);
                } else {
                    showDismissEntry();
                }
            }
            return false;
        } else if (currentPage == PAGE_COVER && !(outputEntry == null || outputEntry.isEditingCover)) {
            processDone();
            navigateTo(PAGE_PREVIEW, true);
            return false;
        } else {
            close(true);
            closeCamera(true);
            return true;
        }
    }

    private void showDismissEntry() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), resourcesProvider);
        builder.setTitle(getString(R.string.DiscardChanges));
        builder.setMessage(getString(R.string.PhotoEditorDiscardAlert));
        if (outputEntry != null && !outputEntry.isEdit) {
            builder.setNeutralButton(getString(outputEntry.isDraft ? R.string.StoryKeepDraft : R.string.StorySaveDraft), (di, i) -> {
                if (outputEntry == null) {
                    return;
                }
                outputEntry.captionEntitiesAllowed = MessagesController.getInstance(currentAccount).storyEntitiesAllowed();
                showSavedDraftHint = !outputEntry.isDraft;
                applyFilter(null);
                applyPaint();
                applyPaintMessage();
                destroyPhotoFilterView();
                StoryEntry storyEntry = outputEntry;
                storyEntry.destroy(true);
                storyEntry.caption = captionEdit.getText();
                outputEntry = null;
                DraftsController drafts = MessagesController.getInstance(currentAccount).getStoriesController().getDraftsController();
                if (storyEntry.isDraft) {
                    drafts.edit(storyEntry);
                } else {
                    drafts.append(storyEntry);
                }
                navigateTo(PAGE_CAMERA, true);
            });
        }
        builder.setPositiveButton(outputEntry != null && outputEntry.isDraft && !outputEntry.isEdit ? getString(R.string.StoryDeleteDraft) : getString(R.string.Discard), (dialogInterface, i) -> {
            if (outputEntry != null && !(outputEntry.isEdit || outputEntry.isRepost && !outputEntry.isRepostMessage) && outputEntry.isDraft) {
                MessagesController.getInstance(currentAccount).getStoriesController().getDraftsController().delete(outputEntry);
                outputEntry = null;
            }
            if (outputEntry != null && (outputEntry.isEdit || outputEntry.isRepost && !outputEntry.isRepostMessage)) {
                close(true);
            } else {
                navigateTo(PAGE_CAMERA, true);
            }
        });
        builder.setNegativeButton(getString(R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
        View positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton instanceof TextView) {
            ((TextView) positiveButton).setTextColor(Theme.getColor(Theme.key_text_RedBold, resourcesProvider));
            positiveButton.setBackground(Theme.createRadSelectorDrawable(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_text_RedBold, resourcesProvider), (int) (0.2f * 255)), 6, 6));
        }
    }

    private boolean noCameraPermission;
    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        Context context = getContext();

        windowView = new WindowView(context);
        if (Build.VERSION.SDK_INT >= 21) {
            windowView.setFitsSystemWindows(false);
            windowView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                    insetTop = 0;
                    insetBottom = 0;
                    insetLeft = 0;
                    insetRight = 0;
                    windowView.requestLayout();
                    if (Build.VERSION.SDK_INT >= 30) {
                        return WindowInsets.CONSUMED;
                    } else {
                        return insets.consumeSystemWindowInsets();
                    }
                }
            });
        }
        windowView.setFocusable(true);

        flashViews = new FlashViews(context, windowManager, windowView, windowLayoutParams);
        flashViews.add(new FlashViews.Invertable() {
            @Override
            public void setInvert(float invert) {
                AndroidUtilities.setLightNavigationBar(windowView, invert > 0.5f);
                AndroidUtilities.setLightStatusBar(windowView, invert > 0.5f);
            }
            @Override
            public void invalidate() {}
        });
        windowView.addView(flashViews.backgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        windowView.addView(containerView = new ContainerView(context));

        containerView.addView(previewContainer = new FrameLayout(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (previewTouchable != null) {
                    previewTouchable.onTouch(event);
                    return true;
                }
                return super.onTouchEvent(event);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);

                if (photoFilterViewCurvesControl != null) {
                    photoFilterViewCurvesControl.setActualArea(0, 0, photoFilterViewCurvesControl.getMeasuredWidth(), photoFilterViewCurvesControl.getMeasuredHeight());
                }
                if (photoFilterViewBlurControl != null) {
                    photoFilterViewBlurControl.setActualAreaSize(photoFilterViewBlurControl.getMeasuredWidth(), photoFilterViewBlurControl.getMeasuredHeight());
                }
            }

            private final Rect leftExclRect = new Rect();
            private final Rect rightExclRect = new Rect();

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    final int w = right - left;
                    final int h = bottom - top;
                    leftExclRect.set(0, h - dp(120), dp(40), h);
                    rightExclRect.set(w - dp(40), h - dp(120), w, h);
                    setSystemGestureExclusionRects(Arrays.asList(leftExclRect, rightExclRect));
                }
            }

            @Override
            public void invalidate() {
                if (cameraAnimationInProgress) {
                    return;
                }
                super.invalidate();
            }

            private RenderNode renderNode;
            @Override
            protected void dispatchDraw(@NonNull Canvas c) {
                boolean endRecording = false;
                Canvas canvas = c;
                if (Build.VERSION.SDK_INT >= 31 && c.isHardwareAccelerated() && !AndroidUtilities.makingGlobalBlurBitmap) {
                    if (renderNode == null) {
                        renderNode = new RenderNode("ChatAttachAlertPhotoLayout.PreviewView");
                    }
                    renderNode.setPosition(0, 0, getWidth(), getHeight());
                    canvas = renderNode.beginRecording();
                    endRecording = true;
                }
                super.dispatchDraw(canvas);
                if (endRecording && Build.VERSION.SDK_INT >= 31) {
                    renderNode.endRecording();
                    if (blurManager != null) {
                        blurManager.setRenderNode(this, renderNode, 0xFF1F1F1F);
                    }
                    c.drawRenderNode(renderNode);
                }
            }
        });
//        containerView.addView(flashViews.foregroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cameraPanel.addView(flashViews.foregroundView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        blurManager = new BlurringShader.BlurManager(previewContainer);
        videoTextureHolder = new PreviewView.TextureViewHolder();
//        containerView.addView(actionBarContainer = new FrameLayout(context)); // 150dp
//        containerView.addView(controlContainer = new FrameLayout(context)); // 220dp
        cameraPanel.addView(actionBarContainer = new FrameLayout(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));
        cameraPanel.addView(controlContainer = new FrameLayout(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        containerView.addView(captionContainer = new FrameLayout(context) {
            @Override
            public void setTranslationY(float translationY) {
                if (getTranslationY() != translationY && captionEdit != null) {
                    super.setTranslationY(translationY);
                    captionEdit.updateMentionsLayoutPosition();
                }
            }
        }); // full height
        captionContainer.setVisibility(View.GONE);
        captionContainer.setAlpha(0f);
//        containerView.addView(navbarContainer = new FrameLayout(context)); // 48dp
        cameraPanel.addView(navbarContainer = new FrameLayout(context), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        Bulletin.addDelegate(windowView, new Bulletin.Delegate() {
            @Override
            public int getTopOffset(int tag) {
                return dp(56);
            }

            @Override
            public int getBottomOffset(int tag) {
                return Bulletin.Delegate.super.getBottomOffset(tag);
            }

            @Override
            public boolean clipWithGradient(int tag) {
                return true;
            }
        });

        collageLayoutView = new CollageLayoutView2(context, blurManager, containerView, resourcesProvider) {
            @Override
            protected void onLayoutUpdate(CollageLayout layout) {
                collageListView.setVisible(false, true);
                if (layout != null && layout.parts.size() > 1) {
                    collageButton.setIcon(new CollageLayoutButton.CollageLayoutDrawable(lastCollageLayout = layout), true);
                    collageButton.setSelected(true, true);
                } else {
                    collageButton.setSelected(false, true);
                }
                updateActionBarButtons(true);
            }
        };
        collageLayoutView.setCancelGestures(windowView::cancelGestures);
        collageLayoutView.setResetState(() -> {
            updateActionBarButtons(true);
        });
        previewContainer.addView(collageLayoutView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        cameraViewThumb = new ImageView(context);
        cameraViewThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cameraViewThumb.setOnClickListener(v -> {
            if (noCameraPermission) {
                requestCameraPermission(true);
            }
        });
        cameraViewThumb.setClickable(true);
//        previewContainer.addView(cameraViewThumb, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        previewContainer.setBackgroundColor(openType == 1 || openType == 0 ? 0 : 0xff1f1f1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            previewContainer.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight(), dp(12));
                }
            });
            previewContainer.setClipToOutline(true);
        }
        photoFilterEnhanceView = new PhotoFilterView.EnhanceView(context, this::createFilterPhotoView);
        previewView = new PreviewView(context, blurManager, videoTextureHolder) {
            @Override
            public boolean additionalTouchEvent(MotionEvent ev) {
                if (captionEdit != null && captionEdit.isRecording()) {
                    return false;
                }
                return photoFilterEnhanceView.onTouch(ev);
            }

            @Override
            public void applyMatrix() {
                super.applyMatrix();
                applyFilterMatrix();
            }

            @Override
            public void onEntityDraggedTop(boolean value) {
                previewHighlight.show(true, value, actionBarContainer);
            }

            @Override
            public void onEntityDraggedBottom(boolean value) {
                previewHighlight.updateCaption(captionEdit.getText());
//                previewHighlight.show(false, value, null);
            }

            @Override
            public void onEntityDragEnd(boolean delete) {
                controlContainer.clearAnimation();
                controlContainer.animate().alpha(1f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
                trash.onDragInfo(false, delete);
                trash.clearAnimation();
                trash.animate().alpha(0f).withEndAction(() -> {
                    trash.setVisibility(View.GONE);
                }).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).setStartDelay(delete ? 500 : 0).start();
                super.onEntityDragEnd(delete);
            }

            @Override
            public void onEntityDragStart() {
                controlContainer.clearAnimation();
                controlContainer.animate().alpha(0f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();

                trash.setVisibility(View.VISIBLE);
                trash.setAlpha(0f);
                trash.clearAnimation();
                trash.animate().alpha(1f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
            }

            @Override
            public void onEntityDragTrash(boolean enter) {
                trash.onDragInfo(enter, false);
            }

            @Override
            protected void onTimeDrag(boolean dragStart, long time, boolean dragEnd) {
                videoTimeView.setTime(time, !dragStart);
                videoTimeView.show(!dragEnd, true);
            }

            @Override
            public void onRoundSelectChange(boolean selected) {
                if (paintView == null) return;
                if (!selected && paintView.getSelectedEntity() instanceof RoundView) {
                    paintView.selectEntity(null);
                } else if (selected && !(paintView.getSelectedEntity() instanceof RoundView) && paintView.findRoundView() != null) {
                    paintView.selectEntity(paintView.findRoundView());
                }
            }

            @Override
            public void onRoundRemove() {
                if (previewView != null) {
                    previewView.setupRound(null, null, true);
                }
                if (paintView != null) {
                    paintView.deleteRound();
                }
                if (captionEdit != null) {
                    captionEdit.setHasRoundVideo(false);
                }
                if (outputEntry != null) {
                    if (outputEntry.round != null) {
                        try {
                            outputEntry.round.delete();
                        } catch (Exception ignore) {}
                        outputEntry.round = null;
                    }
                    if (outputEntry.roundThumb != null) {
                        try {
                            new File(outputEntry.roundThumb).delete();
                        } catch (Exception ignore) {}
                        outputEntry.roundThumb = null;
                    }
                }
            }

            @Override
            protected void invalidateTextureViewHolder() {
                if (outputEntry != null && outputEntry.isRepostMessage && outputEntry.isVideo && paintView != null && paintView.entitiesView != null) {
                    for (int i = 0; i < paintView.entitiesView.getChildCount(); ++i) {
                        View child = paintView.entitiesView.getChildAt(i);
                        if (child instanceof MessageEntityView) {
                            ((MessageEntityView) child).invalidateAll();
                        }
                    }
                }
            }

            @Override
            public void onAudioChanged() {
                if (paintView != null) {
                    paintView.setHasAudio(outputEntry != null && outputEntry.audioPath != null);
                }
            }
        };
        previewView.setCollageView(collageLayoutView);
        previewView.invalidateBlur = this::invalidateBlur;
        previewView.setOnTapListener(() -> {
            if (currentEditMode != EDIT_MODE_NONE || currentPage != PAGE_PREVIEW || captionEdit.keyboardShown || captionEdit != null && captionEdit.isRecording()) {
                return;
            }
            if (timelineView.onBackPressed()) {
                return;
            }
            switchToEditMode(EDIT_MODE_PAINT, true);
            if (paintView != null) {
                paintView.openText();
                paintView.enteredThroughText = true;
            }
        });
        previewView.setVisibility(View.GONE);
        previewView.whenError(() -> {
            videoError = true;
            previewButtons.setShareEnabled(false);
            downloadButton.showFailedVideo();
        });
        previewContainer.addView(previewView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        previewContainer.addView(photoFilterEnhanceView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        captionEdit = new CaptionStory(context, windowView, windowView, containerView, resourcesProvider, blurManager) {
            @Override
            protected boolean ignoreTouches(float x, float y) {
                if (paintView == null || paintView.entitiesView == null || captionEdit.keyboardShown) return false;
                x += captionEdit.getX();
                y += captionEdit.getY();
                x += captionContainer.getX();
                y += captionContainer.getY();
                x -= previewContainer.getX();
                y -= previewContainer.getY();

                for (int i = 0; i < paintView.entitiesView.getChildCount(); ++i) {
                    View view = paintView.entitiesView.getChildAt(i);
                    if (view instanceof EntityView) {
                        org.telegram.ui.Components.Rect rect = ((EntityView) view).getSelectionBounds();
                        AndroidUtilities.rectTmp.set(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
                        if (AndroidUtilities.rectTmp.contains(x, y)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void setVisibility(int visibility) {
                super.setVisibility(visibility);
            }

            @Override
            protected void drawBlurBitmap(Bitmap bitmap, float amount) {
                windowView.drawBlurBitmap(bitmap, amount);
                super.drawBlurBitmap(bitmap, amount);
            }

            @Override
            public boolean captionLimitToast() {
                if (MessagesController.getInstance(currentAccount).premiumFeaturesBlocked()) {
                    return false;
                }
                Bulletin visibleBulletin = Bulletin.getVisibleBulletin();
                if (visibleBulletin != null && visibleBulletin.tag == 2) {
                    return false;
                }
                final int symbols = MessagesController.getInstance(currentAccount).storyCaptionLengthLimitPremium;
                final int times = Math.round((float) symbols / MessagesController.getInstance(currentAccount).storyCaptionLengthLimitDefault);
                SpannableStringBuilder text = AndroidUtilities.replaceTags(LocaleController.formatPluralString("CaptionPremiumSubtitle", times, "" + symbols));
                int startIndex = text.toString().indexOf("__");
                if (startIndex >= 0) {
                    text.replace(startIndex, startIndex + 2, "");
                    int endIndex = text.toString().indexOf("__");
                    if (endIndex >= 0) {
                        text.replace(endIndex, endIndex + 2, "");
                        text.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messageLinkIn, resourcesProvider)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        text.setSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                openPremium();
                            }
                        }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                Bulletin bulletin = BulletinFactory.of(captionContainer, resourcesProvider).createSimpleBulletin(R.raw.caption_limit, getString(R.string.CaptionPremiumTitle), text);
                bulletin.tag = 2;
                bulletin.setDuration(5000);
                bulletin.show(false);
                return true;
            }

            @Override
            protected void onCaptionLimitUpdate(boolean overLimit) {
                previewButtons.setShareEnabled(!videoError && !overLimit && (!MessagesController.getInstance(currentAccount).getStoriesController().hasStoryLimit() || (outputEntry != null && outputEntry.isEdit)));
            }

            @Override
            public boolean canRecord() {
                return requestAudioPermission();
            }

            @Override
            public void putRecorder(RoundVideoRecorder recorder) {
                if (currentRoundRecorder != null) {
                    currentRoundRecorder.destroy(true);
                }
                if (previewView != null) {
                    previewView.mute(true);
                    previewView.seek(0);
                }
                recorder.onDone((file, thumb, duration) -> {
                    if (previewView != null) {
                        previewView.mute(false);
                        previewView.seek(0);
                    }
                    if (outputEntry != null) {
                        outputEntry.round = file;
                        outputEntry.roundThumb = thumb;
                        outputEntry.roundDuration = duration;
                        outputEntry.roundLeft = 0;
                        outputEntry.roundRight = 1;
                        outputEntry.roundOffset = 0;
                        outputEntry.roundVolume = 1f;

                        createPhotoPaintView();
                        if (previewView != null && paintView != null) {
                            RoundView roundView = paintView.createRound(outputEntry.roundThumb, true);
                            setHasRoundVideo(true);
                            previewView.setupRound(outputEntry, roundView, true);

                            recorder.hideTo(roundView);
                        } else {
                            recorder.destroy(false);
                        }
                    }
                });
                recorder.onDestroy(() -> {
                    if (previewView != null) {
                        previewView.mute(false);
                        previewView.seek(0);
                    }
                });
                previewContainer.addView(currentRoundRecorder = recorder, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            }

            @Override
            public void removeRound() {
                if (previewView != null) {
                    previewView.setupRound(null, null, true);
                }
                if (paintView != null) {
                    paintView.deleteRound();
                }
                if (captionEdit != null) {
                    captionEdit.setHasRoundVideo(false);
                }
                if (outputEntry != null) {
                    if (outputEntry.round != null) {
                        try {
                            outputEntry.round.delete();
                        } catch (Exception ignore) {}
                        outputEntry.round = null;
                    }
                    if (outputEntry.roundThumb != null) {
                        try {
                            new File(outputEntry.roundThumb).delete();
                        } catch (Exception ignore) {}
                        outputEntry.roundThumb = null;
                    }
                }
            }

            @Override
            public void invalidateDrawOver2() {
                if (captionEditOverlay != null) {
                    captionEditOverlay.invalidate();
                }
            }

            @Override
            public boolean drawOver2FromParent() {
                return true;
            }

            @Override
            public int getTimelineHeight() {
                if (videoTimelineContainerView != null && timelineView != null && timelineView.getVisibility() == View.VISIBLE) {
                    return timelineView.getTimelineHeight();
                }
                return 0;
            }

            @Override
            protected boolean customBlur() {
                return blurManager.hasRenderNode();
            }

            private final Path path = new Path();
            @Override
            protected void drawBlur(BlurringShader.StoryBlurDrawer blur, Canvas canvas, RectF rect, float r, boolean text, float ox, float oy, boolean thisView, float alpha) {
                if (!canvas.isHardwareAccelerated()) {
                    return;
                }
                canvas.save();
                path.rewind();
                path.addRoundRect(rect, r, r, Path.Direction.CW);
                canvas.clipPath(path);
                canvas.translate(ox, oy);
                blur.drawRect(canvas, 0, 0, alpha);
                canvas.restore();
            }
        };
        captionEdit.setAccount(UserConfig.selectedAccount);
        captionEdit.setUiBlurBitmap(this::getUiBlurBitmap);
        Bulletin.addDelegate(captionContainer, new Bulletin.Delegate() {
            @Override
            public int getBottomOffset(int tag) {
                return captionEdit.getEditTextHeight() + AndroidUtilities.dp(12);
            }
        });
        captionEdit.setOnHeightUpdate(height -> {
            if (videoTimelineContainerView != null) {
                videoTimelineContainerView.setTranslationY(currentEditMode == EDIT_MODE_TIMELINE ? dp(68) : -(captionEdit.getEditTextHeight() + dp(12)) + dp(64));
            }
            Bulletin visibleBulletin = Bulletin.getVisibleBulletin();
            if (visibleBulletin != null && visibleBulletin.tag == 2) {
                visibleBulletin.updatePosition();
            }
        });
        captionEdit.setOnPeriodUpdate(period -> {
            if (outputEntry != null) {
                outputEntry.period = period;
                MessagesController.getGlobalMainSettings().edit().putInt("story_period", period).apply();
//                privacySelector.setStoryPeriod(period);
            }
        });
        if (photoViewerProvider.getDialogId() != 0) {
            captionEdit.setDialogId(photoViewerProvider.getDialogId());
        }
        captionEdit.setOnPremiumHint(this::showPremiumPeriodBulletin);
        captionEdit.setOnKeyboardOpen(open -> {
            if (open && timelineView != null) {
                timelineView.onBackPressed();
            }
            previewView.updatePauseReason(2, open);
            videoTimelineContainerView.clearAnimation();
            videoTimelineContainerView.animate().alpha(open ? 0f : 1f).setDuration(120).start();
            Bulletin visibleBulletin = Bulletin.getVisibleBulletin();
            if (visibleBulletin != null && visibleBulletin.tag == 2) {
                visibleBulletin.updatePosition();
            }
        });
        captionEditOverlay = new View(context) {
            @Override
            protected void dispatchDraw(Canvas canvas) {
                canvas.save();
                canvas.translate(captionContainer.getX() + captionEdit.getX(), captionContainer.getY() + captionEdit.getY());
                captionEdit.drawOver2(canvas, captionEdit.getBounds(), captionEdit.getOver2Alpha());
                canvas.restore();
            }
        };
        containerView.addView(captionEditOverlay);

        timelineView = new TimelineView(context, containerView, previewContainer, resourcesProvider, blurManager);
        timelineView.setOnTimelineClick(() -> {
            if (currentPage != PAGE_PREVIEW) return;
            switchToEditMode(EDIT_MODE_TIMELINE, true);
        });
        previewView.setVideoTimelineView(timelineView);
        timelineView.setVisibility(View.GONE);
        timelineView.setAlpha(0f);
        videoTimelineContainerView = new FrameLayout(context);
        videoTimelineContainerView.addView(timelineView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, TimelineView.heightDp(), Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 0));
        videoTimeView = new VideoTimeView(context);
        videoTimeView.setVisibility(View.GONE);
        videoTimeView.show(false, false);
        videoTimelineContainerView.addView(videoTimeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 25, Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0, 0, 0));
        captionContainer.addView(videoTimelineContainerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, TimelineView.heightDp() + 25, Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 68));
        captionContainer.addView(captionEdit, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 200, 0, 0));
        collageLayoutView.setTimelineView(timelineView);
        collageLayoutView.setPreviewView(previewView);
        containerView.addView(cameraPanel,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));

        coverTimelineView = new TimelineView(context, containerView, previewContainer, resourcesProvider, blurManager);
        coverTimelineView.setCover();
        coverTimelineView.setVisibility(View.GONE);
        coverTimelineView.setAlpha(0f);
        captionContainer.addView(coverTimelineView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, TimelineView.heightDp(), Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 6));

        backButton = new FlashViews.ImageViewInvertable(context);
        backButton.setContentDescription(getString(R.string.AccDescrGoBack));
        backButton.setScaleType(ImageView.ScaleType.CENTER);
        backButton.setImageResource(R.drawable.ic_close_white);
        backButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        backButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
        backButton.setOnClickListener(e -> {
            if (awaitingPlayer) {
                return;
            }
            onBackPressedChecked();
        });
        actionBarContainer.addView(backButton, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.LEFT));
        flashViews.add(backButton);

        titleTextView = new SimpleTextView(context);
        titleTextView.setTextSize(20);
        titleTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        titleTextView.setTextColor(0xffffffff);
        titleTextView.setTypeface(AndroidUtilities.bold());
        titleTextView.setText(getString(R.string.RecorderNewStory));
        titleTextView.getPaint().setShadowLayer(dpf2(1), 0, 1, 0x40000000);
        titleTextView.setAlpha(0f);
        titleTextView.setVisibility(View.GONE);
        titleTextView.setEllipsizeByGradient(true);
        titleTextView.setRightPadding(AndroidUtilities.dp(144));
        actionBarContainer.addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.TOP | Gravity.FILL_HORIZONTAL, 71, 0, 0, 0));

        actionBarButtons = new LinearLayout(context);
        actionBarButtons.setOrientation(LinearLayout.HORIZONTAL);
        actionBarButtons.setGravity(Gravity.RIGHT);
        actionBarContainer.addView(actionBarButtons, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.RIGHT | Gravity.FILL_HORIZONTAL, 0, 0, 8, 0));

        downloadButton = new DownloadButton(context, done -> {
            applyPaint();
            applyPaintMessage();
            applyFilter(done);
        }, currentAccount, windowView, resourcesProvider);

        muteHint = new HintView2(context, HintView2.DIRECTION_TOP)
                .setJoint(1, -77 + 8 - 2)
                .setDuration(2000)
                .setBounce(false)
                .setAnimatedTextHacks(true, true, false);
        muteHint.setPadding(dp(8), 0, dp(8), 0);
        actionBarContainer.addView(muteHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 52, 0, 0));

        muteButton = new RLottieImageView(context);
        muteButton.setScaleType(ImageView.ScaleType.CENTER);
        muteButton.setImageResource(outputEntry != null && outputEntry.muted ? R.drawable.media_unmute : R.drawable.media_mute);
        muteButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        muteButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
        muteButton.setOnClickListener(e -> {
            if (outputEntry == null || awaitingPlayer) {
                return;
            }
            outputEntry.muted = !outputEntry.muted;
            if (outputEntry.collageContent != null) {
                for (StoryEntry entry : outputEntry.collageContent) {
                    entry.muted = outputEntry.muted;
                }
            }
            final boolean hasMusic = !TextUtils.isEmpty(outputEntry.audioPath);
            final boolean hasRound = outputEntry.round != null;
            if (currentEditMode == EDIT_MODE_NONE) {
                muteHint.setText(
                        outputEntry.muted ?
                                getString(hasMusic || hasRound ? R.string.StoryOriginalSoundMuted : R.string.StorySoundMuted) :
                                getString(hasMusic || hasRound ? R.string.StoryOriginalSoundNotMuted : R.string.StorySoundNotMuted),
                        muteHint.shown()
                );
                muteHint.show();
            }
            setIconMuted(outputEntry.muted, true);
            previewView.checkVolumes();
        });
        muteButton.setVisibility(View.GONE);
        muteButton.setAlpha(0f);

        playButton = new PlayPauseButton(context);
        playButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
        playButton.setVisibility(View.GONE);
        playButton.setAlpha(0f);
        playButton.setOnClickListener(e -> {
            boolean playing = previewView.isPlaying();
            previewView.play(!playing);
            playButton.drawable.setPause(!playing, true);
        });

        actionBarButtons.addView(playButton, LayoutHelper.createLinear(46, 56, Gravity.TOP | Gravity.RIGHT));
        actionBarButtons.addView(muteButton, LayoutHelper.createLinear(46, 56, Gravity.TOP | Gravity.RIGHT));
        actionBarButtons.addView(downloadButton, LayoutHelper.createFrame(46, 56, Gravity.TOP | Gravity.RIGHT));

        flashButton = new ToggleButton2(context);
        flashButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
        flashButton.setOnClickListener(e -> {
            if (cameraView == null || awaitingPlayer) {
                return;
            }
            String current = getCurrentFlashMode();
            String next = getNextFlashMode();
            if (current == null || current.equals(next)) {
                return;
            }
            setCurrentFlashMode(next);
            setCameraFlashModeIcon(next, true);
        });
        flashButton.setOnLongClickListener(e -> {
            if (cameraView == null || !cameraView.isFrontface()) {
                return false;
            }

            checkFrontfaceFlashModes();
            flashButton.setSelected(true);
            flashViews.previewStart();
            ItemOptions.makeOptions(containerView, resourcesProvider, flashButton)
                    .addView(
                            new SliderView(getContext(), SliderView.TYPE_WARMTH)
                                    .setValue(flashViews.warmth)
                                    .setOnValueChange(v -> {
                                        flashViews.setWarmth(v);
                                    })
                    )
                    .addSpaceGap()
                    .addView(
                            new SliderView(getContext(), SliderView.TYPE_INTENSITY)
                                    .setMinMax(.65f, 1f)
                                    .setValue(flashViews.intensity)
                                    .setOnValueChange(v -> {
                                        flashViews.setIntensity(v);
                                    })
                    )
                    .setOnDismiss(() -> {
                        saveFrontFaceFlashMode();
                        flashViews.previewEnd();
                        flashButton.setSelected(false);
                    })
                    .setDimAlpha(0)
                    .setGravity(Gravity.RIGHT)
                    .translate(dp(46), -dp(4))
                    .setBackgroundColor(0xbb1b1b1b)
                    .show();
            return true;
        });
        flashButton.setVisibility(View.GONE);
        flashButton.setAlpha(0f);
        flashViews.add(flashButton);
        actionBarContainer.addView(flashButton, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.RIGHT));

        dualButton = new ToggleButton(context, R.drawable.media_dual_camera2_shadow, R.drawable.media_dual_camera2);
        dualButton.setOnClickListener(v -> {
            if (cameraView == null || currentPage != PAGE_CAMERA) {
                return;
            }
            cameraView.toggleDual();
            dualButton.setValue(cameraView.isDual());

            dualHint.hide();
            MessagesController.getGlobalMainSettings().edit().putInt("storydualhint", 2).apply();
            if (savedDualHint.shown()) {
                MessagesController.getGlobalMainSettings().edit().putInt("storysvddualhint", 2).apply();
            }
            savedDualHint.hide();
        });
        final boolean dualCameraAvailable = DualCameraView.dualAvailableStatic(context);
        dualButton.setVisibility(dualCameraAvailable ? View.VISIBLE : View.GONE);
        dualButton.setAlpha(dualCameraAvailable ? 1.0f : 0.0f);
        flashViews.add(dualButton);
        actionBarContainer.addView(dualButton, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.RIGHT));

        collageButton = new CollageLayoutButton(context);
        collageButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
        if (lastCollageLayout == null) {
            lastCollageLayout = CollageLayout.getLayouts().get(6);
        }
        collageButton.setOnClickListener(v -> {
            if (currentPage != PAGE_CAMERA || animatedRecording) return;
            if (cameraView != null && cameraView.isDual()) {
                cameraView.toggleDual();
            }
            if (!collageListView.isVisible() && !collageLayoutView.hasLayout()) {
                collageLayoutView.setLayout(lastCollageLayout, true);
                collageListView.setSelected(lastCollageLayout);
                collageButton.setIcon(new CollageLayoutButton.CollageLayoutDrawable(lastCollageLayout), true);
                collageButton.setSelected(true);
                if (cameraView != null) {
                    cameraView.recordHevc = !collageLayoutView.hasLayout();
                }
            }
            collageListView.setVisible(!collageListView.isVisible(), true);
            updateActionBarButtons(true);
        });
        collageButton.setIcon(new CollageLayoutButton.CollageLayoutDrawable(lastCollageLayout), false);
        collageButton.setSelected(false);
        collageButton.setVisibility(View.VISIBLE);
        collageButton.setAlpha(1.0f);
        flashViews.add(collageButton);
        actionBarContainer.addView(collageButton, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.RIGHT));

        collageRemoveButton = new ToggleButton2(context);
        collageRemoveButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
        collageRemoveButton.setIcon(new CollageLayoutButton.CollageLayoutDrawable(new CollageLayout("../../.."), true), false);
        collageRemoveButton.setVisibility(View.GONE);
        collageRemoveButton.setAlpha(0.0f);
        collageRemoveButton.setOnClickListener(v -> {
            collageLayoutView.setLayout(null, true);
            collageLayoutView.clear(true);
            collageListView.setSelected(null);
            if (cameraView != null) {
                cameraView.recordHevc = !collageLayoutView.hasLayout();
            }
            collageListView.setVisible(false, true);
            updateActionBarButtons(true);
        });
        flashViews.add(collageRemoveButton);
        actionBarContainer.addView(collageRemoveButton, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.RIGHT));

        collageListView = new CollageLayoutButton.CollageLayoutListView(context, flashViews);
        collageListView.listView.scrollToPosition(6);
        collageListView.setSelected(null);
        collageListView.setOnLayoutClick(layout -> {
            collageLayoutView.setLayout(lastCollageLayout = layout, true);
            collageListView.setSelected(layout);
            if (cameraView != null) {
                cameraView.recordHevc = !collageLayoutView.hasLayout();
            }
            collageButton.setDrawable(new CollageLayoutButton.CollageLayoutDrawable(layout));
            setActionBarButtonVisible(collageRemoveButton, collageListView.isVisible(), true);
            recordControl.setCollageProgress(collageLayoutView.hasLayout() ? collageLayoutView.getFilledProgress() : 0.0f, true);
        });
        actionBarContainer.addView(collageListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.TOP | Gravity.RIGHT));

        dualHint = new HintView2(context, HintView2.DIRECTION_TOP)
                .setJoint(1, -20)
                .setDuration(5000)
                .setCloseButton(true)
                .setText(getString(R.string.StoryCameraDualHint))
                .setOnHiddenListener(() -> MessagesController.getGlobalMainSettings().edit().putInt("storydualhint", MessagesController.getGlobalMainSettings().getInt("storydualhint", 0) + 1).apply());
        dualHint.setPadding(dp(8), 0, dp(8), 0);
        actionBarContainer.addView(dualHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 52, 0, 0));

        savedDualHint = new HintView2(context, HintView2.DIRECTION_RIGHT)
                .setJoint(0, 56 / 2)
                .setDuration(5000)
                .setMultilineText(true);
        actionBarContainer.addView(savedDualHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 52, 0));

        removeCollageHint = new HintView2(context, HintView2.DIRECTION_TOP)
                .setJoint(1, -20)
                .setDuration(5000)
                .setText(LocaleController.getString(R.string.StoryCollageRemoveGrid));
        removeCollageHint.setPadding(dp(8), 0, dp(8), 0);
        actionBarContainer.addView(removeCollageHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 52, 0, 0));

        videoTimerView = new VideoTimerView(context);
        showVideoTimer(false, false);
        actionBarContainer.addView(videoTimerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 45, Gravity.TOP | Gravity.FILL_HORIZONTAL, 56, 0, 56, 0));
        flashViews.add(videoTimerView);

        if (Build.VERSION.SDK_INT >= 21) {
            MediaController.loadGalleryPhotosAlbums(0);
        }

        recordControl = new RecordControl(context);
        recordControl.setDelegate(recordControlDelegate);
        recordControl.startAsVideo(isVideo);
        controlContainer.addView(recordControl, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 100, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        flashViews.add(recordControl);
        recordControl.setCollageProgress(collageLayoutView.hasLayout() ? collageLayoutView.getFilledProgress() : 0.0f, true);

        cameraHint = new HintView2(context, HintView2.DIRECTION_BOTTOM)
                .setMultilineText(true)
                .setText(getString(R.string.StoryCameraHint2))
                .setMaxWidth(320)
                .setDuration(5000L)
                .setTextAlign(Layout.Alignment.ALIGN_CENTER);
        controlContainer.addView(cameraHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM, 0, 0, 0, 100));

        zoomControlView = new ZoomControlView(context);
        zoomControlView.enabledTouch = false;
        zoomControlView.setAlpha(0.0f);
        controlContainer.addView(zoomControlView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 100 + 8));
        zoomControlView.setDelegate(zoom -> {
            if (cameraView != null) {
                cameraView.setZoom(cameraZoom = zoom);
            }
            showZoomControls(true, true);
        });
        zoomControlView.setZoom(cameraZoom = 0, false);

        modeSwitcherView = new PhotoVideoSwitcherView(context) {
            @Override
            protected boolean allowTouch() {
                return !inCheck();
            }
        };
        modeSwitcherView.setOnSwitchModeListener(newIsVideo -> {
            if (takingPhoto || takingVideo) {
                return;
            }

            isVideo = newIsVideo;
            showVideoTimer(isVideo && !collageListView.isVisible(), true);
            modeSwitcherView.switchMode(isVideo);
            recordControl.startAsVideo(isVideo);
        });
        modeSwitcherView.setOnSwitchingModeListener(t -> {
            recordControl.startAsVideoT(t);
        });
        navbarContainer.addView(modeSwitcherView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        flashViews.add(modeSwitcherView);

        hintTextView = new HintTextView(context);
        navbarContainer.addView(hintTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 32, Gravity.CENTER, 8, 0, 8, 8));
        flashViews.add(hintTextView);

        collageHintTextView = new HintTextView(context);
        collageHintTextView.setText(LocaleController.getString(R.string.StoryCollageReorderHint), false);
        collageHintTextView.setAlpha(0.0f);
        navbarContainer.addView(collageHintTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 32, Gravity.CENTER, 8, 0, 8, 8));
        flashViews.add(collageHintTextView);

        coverButton = new ButtonWithCounterView(context, resourcesProvider);
        coverButton.setVisibility(View.GONE);
        coverButton.setAlpha(0f);
        coverButton.setText(LocaleController.getString(R.string.StoryCoverSave), false);
        coverButton.setOnClickListener(v -> {
            if (outputEntry == null) {
                return;
            }
            outputEntry.coverSet = true;
            outputEntry.cover = coverValue;
            processDone();
            if (outputEntry != null && !outputEntry.isEditingCover) {
                AndroidUtilities.runOnUIThread(() -> {
                    if (!outputEntry.isEditingCover && privacySheet != null && previewView != null) {
                        previewView.getCoverBitmap(bitmap -> {
                            if (outputEntry == null) return;
                            AndroidUtilities.recycleBitmap(outputEntry.coverBitmap);
                            outputEntry.coverBitmap = bitmap;
                            if (privacySheet == null) return;
                            privacySheet.setCover(outputEntry.coverBitmap);
                        }, previewView, paintViewRenderView, paintViewEntitiesView);
                    }
                    navigateTo(PAGE_PREVIEW, true);
                }, 400);
            }
        });
        navbarContainer.addView(coverButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 10, 10, 10, 10));

        previewButtons = new PreviewButtons(context);
        previewButtons.setVisibility(View.GONE);
        previewButtons.setOnClickListener((Integer btn) -> {
            if (outputEntry == null || captionEdit.isRecording()) {
                return;
            }
            captionEdit.clearFocus();
            if (btn == PreviewButtons.BUTTON_SHARE) {
                processDone();
            } else if (btn == PreviewButtons.BUTTON_PAINT) {
                switchToEditMode(EDIT_MODE_PAINT, true);
                if (paintView != null) {
                    paintView.enteredThroughText = false;
                    paintView.openPaint();
                }
            } else if (btn == PreviewButtons.BUTTON_TEXT) {
                switchToEditMode(EDIT_MODE_PAINT, true);
                if (paintView != null) {
                    paintView.openText();
                    paintView.enteredThroughText = true;
                }
            } else if (btn == PreviewButtons.BUTTON_STICKER) {
                createPhotoPaintView();
                hidePhotoPaintView();
                if (paintView != null) {
                    paintView.openStickers();
                }
            } else if (btn == PreviewButtons.BUTTON_ADJUST) {
                switchToEditMode(EDIT_MODE_FILTER, true);
            }
        });
        navbarContainer.addView(previewButtons, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 52, Gravity.CENTER_VERTICAL | Gravity.FILL_HORIZONTAL));

        trash = new TrashView(context);
        trash.setAlpha(0f);
        trash.setVisibility(View.GONE);
        previewContainer.addView(trash, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 120, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

        previewHighlight = new PreviewHighlightView(context, currentAccount, resourcesProvider);
        previewContainer.addView(previewHighlight, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));
        updateActionBarButtonsOffsets();
    }

    private void updateActionBarButtonsOffsets() {
        float right = 0;
        collageRemoveButton.setTranslationX(-right); right += dp(46) * collageRemoveButton.getAlpha();
        dualButton.setTranslationX(-right);          right += dp(46) * dualButton.getAlpha();
        collageButton.setTranslationX(-right);       right += dp(46) * collageButton.getAlpha();
        flashButton.setTranslationX(-right);         right += dp(46) * flashButton.getAlpha();

        float left = 0;
        backButton.setTranslationX(left); left += dp(46) * backButton.getAlpha();

        collageListView.setBounds(left + dp(8), right + dp(8));
    }

    public void navigateTo(int page, boolean animated) {
        if (page == currentPage) {
            return;
        }

        final int oldPage = currentPage;
        currentPage = page;

        if (pageAnimator != null) {
            pageAnimator.cancel();
        }

        onNavigateStart(oldPage, page);
        if (previewButtons != null) {
            previewButtons.appear(page == PAGE_PREVIEW, animated);
        }
        showVideoTimer(page == PAGE_CAMERA && isVideo && !collageListView.isVisible() && !inCheck(), animated);
        if (page != PAGE_PREVIEW) {
            videoTimeView.show(false, animated);
        }
        setActionBarButtonVisible(backButton, !collageListView.isVisible(), animated);
        setActionBarButtonVisible(flashButton, page == PAGE_CAMERA && !collageListView.isVisible() && flashButtonMode != null && !inCheck(), animated);
        setActionBarButtonVisible(dualButton, page == PAGE_CAMERA && cameraView != null && cameraView.dualAvailable() && !collageListView.isVisible() && !collageLayoutView.hasLayout(), true);
        setActionBarButtonVisible(collageButton, page == PAGE_CAMERA && !collageListView.isVisible(), animated);
        updateActionBarButtons(animated);
        if (animated) {
            pageAnimator = new AnimatorSet();

            ArrayList<Animator> animators = new ArrayList<>();

            if (cameraView != null) {
                animators.add(ObjectAnimator.ofFloat(cameraView, View.ALPHA, page == PAGE_CAMERA ? 1 : 0));
            }
            cameraViewThumb.setVisibility(View.VISIBLE);
            animators.add(ObjectAnimator.ofFloat(cameraViewThumb, View.ALPHA, page == PAGE_CAMERA ? 1 : 0));
            animators.add(ObjectAnimator.ofFloat(previewView, View.ALPHA, page == PAGE_PREVIEW && !collageLayoutView.hasLayout() || page == PAGE_COVER ? 1 : 0));
            animators.add(ObjectAnimator.ofFloat(collageLayoutView, View.ALPHA, page == PAGE_CAMERA || page == PAGE_PREVIEW && collageLayoutView.hasLayout() ? 1 : 0));

            animators.add(ObjectAnimator.ofFloat(recordControl, View.ALPHA, page == PAGE_CAMERA ? 1 : 0));
//            animators.add(ObjectAnimator.ofFloat(flashButton, View.ALPHA, page == PAGE_CAMERA ? 1 : 0));
//            animators.add(ObjectAnimator.ofFloat(dualButton, View.ALPHA, page == PAGE_CAMERA && cameraView != null && cameraView.dualAvailable() ? 1 : 0));
            animators.add(ObjectAnimator.ofFloat(recordControl, View.TRANSLATION_Y, page == PAGE_CAMERA ? 0 : dp(24)));
            animators.add(ObjectAnimator.ofFloat(modeSwitcherView, View.ALPHA, page == PAGE_CAMERA && !inCheck() ? 1 : 0));
            animators.add(ObjectAnimator.ofFloat(modeSwitcherView, View.TRANSLATION_Y, page == PAGE_CAMERA && !inCheck() ? 0 : dp(24)));
//            backButton.setVisibility(View.VISIBLE);
//            animators.add(ObjectAnimator.ofFloat(backButton, View.ALPHA, 1));
            animators.add(ObjectAnimator.ofFloat(hintTextView, View.ALPHA, page == PAGE_CAMERA && animatedRecording && !inCheck() ? 1 : 0));
            animators.add(ObjectAnimator.ofFloat(collageHintTextView, View.ALPHA, page == PAGE_CAMERA && !animatedRecording && inCheck() ? 0.6f : 0));
            animators.add(ObjectAnimator.ofFloat(captionContainer, View.ALPHA, page == PAGE_PREVIEW && (outputEntry == null || outputEntry.botId == 0) || page == PAGE_COVER ? 1f : 0));
            animators.add(ObjectAnimator.ofFloat(captionContainer, View.TRANSLATION_Y, page == PAGE_PREVIEW && (outputEntry == null || outputEntry.botId == 0) || page == PAGE_COVER ? 0 : dp(12)));
            animators.add(ObjectAnimator.ofFloat(captionEdit, View.ALPHA, page == PAGE_COVER ? 0f : 1f));
            animators.add(ObjectAnimator.ofFloat(titleTextView, View.ALPHA, page == PAGE_PREVIEW || page == PAGE_COVER ? 1f : 0));
            animators.add(ObjectAnimator.ofFloat(coverButton, View.ALPHA, page == PAGE_COVER ? 1f : 0f));

            animators.add(ObjectAnimator.ofFloat(timelineView, View.ALPHA, page == PAGE_PREVIEW ? 1f : 0));
            animators.add(ObjectAnimator.ofFloat(coverTimelineView, View.ALPHA, page == PAGE_COVER ? 1f : 0));

            animators.add(ObjectAnimator.ofFloat(muteButton, View.ALPHA, page == PAGE_PREVIEW && isVideo ? 1f : 0));
            animators.add(ObjectAnimator.ofFloat(playButton, View.ALPHA, page == PAGE_PREVIEW && (isVideo || outputEntry != null && !TextUtils.isEmpty(outputEntry.audioPath)) ? 1f : 0));
            animators.add(ObjectAnimator.ofFloat(downloadButton, View.ALPHA, page == PAGE_PREVIEW ? 1f : 0));
            if (themeButton != null) {
                animators.add(ObjectAnimator.ofFloat(themeButton, View.ALPHA, page == PAGE_PREVIEW && (outputEntry != null && outputEntry.isRepostMessage) ? 1f : 0));
            }
//            animators.add(ObjectAnimator.ofFloat(privacySelector, View.ALPHA, page == PAGE_PREVIEW ? 1f : 0));

            animators.add(ObjectAnimator.ofFloat(zoomControlView, View.ALPHA, 0));

            pageAnimator.playTogether(animators);
            pageAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onNavigateEnd(oldPage, page);
                }
            });
            pageAnimator.setDuration(460);
            pageAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            pageAnimator.start();
        } else {
            if (cameraView != null) {
                cameraView.setAlpha(page == PAGE_CAMERA ? 1 : 0);
            }
            cameraViewThumb.setAlpha(page == PAGE_CAMERA ? 1f : 0);
            cameraViewThumb.setVisibility(page == PAGE_CAMERA ? View.VISIBLE : View.GONE);
            previewView.setAlpha(page == PAGE_PREVIEW && !collageLayoutView.hasLayout() || page == PAGE_COVER ? 1f : 0);
            collageLayoutView.setAlpha(page == PAGE_CAMERA || page == PAGE_PREVIEW && collageLayoutView.hasLayout() ? 1 : 0);
            recordControl.setAlpha(page == PAGE_CAMERA ? 1f : 0);
            recordControl.setTranslationY(page == PAGE_CAMERA ? 0 : dp(16));
            modeSwitcherView.setAlpha(page == PAGE_CAMERA && !inCheck() ? 1f : 0);
            modeSwitcherView.setTranslationY(page == PAGE_CAMERA && !inCheck() ? 0 : dp(16));
            hintTextView.setAlpha(page == PAGE_CAMERA && animatedRecording && !inCheck() ? 1f : 0);
            collageHintTextView.setAlpha(page == PAGE_CAMERA && !animatedRecording && inCheck() ? 0.6f : 0);
            captionContainer.setAlpha(page == PAGE_PREVIEW || page == PAGE_COVER ? 1f : 0);
            captionContainer.setTranslationY(page == PAGE_PREVIEW || page == PAGE_COVER ? 0 : dp(12));
            captionEdit.setAlpha(page == PAGE_COVER ? 0f : 1f);
            muteButton.setAlpha(page == PAGE_PREVIEW && isVideo ? 1f : 0);
            playButton.setAlpha(page == PAGE_PREVIEW && (isVideo || outputEntry != null && !TextUtils.isEmpty(outputEntry.audioPath)) ? 1f : 0);
            downloadButton.setAlpha(page == PAGE_PREVIEW ? 1f : 0);
            if (themeButton != null) {
                themeButton.setAlpha(page == PAGE_PREVIEW && (outputEntry != null && outputEntry.isRepostMessage) ? 1f : 0);
            }
//            privacySelector.setAlpha(page == PAGE_PREVIEW ? 1f : 0);
            timelineView.setAlpha(page == PAGE_PREVIEW ? 1f : 0);
            coverTimelineView.setAlpha(page == PAGE_COVER ? 1f : 0f);
            titleTextView.setAlpha(page == PAGE_PREVIEW || page == PAGE_COVER ? 1f : 0f);
            coverButton.setAlpha(page == PAGE_COVER ? 1f : 0f);
            onNavigateEnd(oldPage, page);
        }
    }

    private Drawable getCameraThumb() {
        Bitmap bitmap = null;
        try {
            File file = new File(ApplicationLoader.getFilesDirFixed(), "cthumb.jpg");
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Throwable ignore) {}
        if (bitmap != null) {
            return new BitmapDrawable(bitmap);
        } else {
            return getContext().getResources().getDrawable(R.drawable.icplaceholder);
        }
    }

    private Runnable afterPlayerAwait;
    private boolean previewAlreadySet;
    public void navigateToPreviewWithPlayerAwait(Runnable open, long seekTo) {
        navigateToPreviewWithPlayerAwait(open, seekTo, 800);
    }
    public void navigateToPreviewWithPlayerAwait(Runnable open, long seekTo, long ms) {
        if (awaitingPlayer || outputEntry == null) {
            return;
        }
        if (afterPlayerAwait != null) {
            AndroidUtilities.cancelRunOnUIThread(afterPlayerAwait);
        }
        previewAlreadySet = true;
        awaitingPlayer = true;
        afterPlayerAwait = () -> {
            animateGalleryListView(false);
            AndroidUtilities.cancelRunOnUIThread(afterPlayerAwait);
            afterPlayerAwait = null;
            awaitingPlayer = false;
            open.run();
        };
        previewView.setAlpha(0f);
        previewView.setVisibility(View.VISIBLE);
        previewView.set(outputEntry, afterPlayerAwait, seekTo);
        previewView.setupAudio(outputEntry, false);
        AndroidUtilities.runOnUIThread(afterPlayerAwait, ms);
    }

    private ValueAnimator galleryOpenCloseAnimator;
    private SpringAnimation galleryOpenCloseSpringAnimator;
    private Boolean galleryListViewOpening;
    private Runnable galleryLayouted;
    private void animateGalleryListView(boolean open) {
        wasGalleryOpen = open;
        if (galleryListViewOpening != null && galleryListViewOpening == open) {
            return;
        }

        if (galleryListView == null) {
            if (open) {
                createGalleryListView();
            }
            if (galleryListView == null) {
                return;
            }
        }

        if (galleryListView.firstLayout) {
            galleryLayouted = () -> animateGalleryListView(open);
            return;
        }

        if (galleryOpenCloseAnimator != null) {
            galleryOpenCloseAnimator.cancel();
            galleryOpenCloseAnimator = null;
        }
        if (galleryOpenCloseSpringAnimator != null) {
            galleryOpenCloseSpringAnimator.cancel();
            galleryOpenCloseSpringAnimator = null;
        }

        if (galleryListView == null) {
            if (open) {
                createGalleryListView();
            }
            if (galleryListView == null) {
                return;
            }
        }
        if (galleryListView != null) {
            galleryListView.ignoreScroll = false;
        }

        if (open && draftSavedHint != null) {
            draftSavedHint.hide(true);
        }

        galleryListViewOpening = open;

        float from = galleryListView.getTranslationY();
        float to = open ? 0 : windowView.getHeight() - galleryListView.top() + AndroidUtilities.navigationBarHeight * 2.5f;
        float fulldist = Math.max(1, windowView.getHeight());

        galleryListView.ignoreScroll = !open;

        applyContainerViewTranslation2 = containerViewBackAnimator == null;
        if (open) {
            galleryOpenCloseSpringAnimator = new SpringAnimation(galleryListView, DynamicAnimation.TRANSLATION_Y, to);
            galleryOpenCloseSpringAnimator.getSpring().setDampingRatio(0.75f);
            galleryOpenCloseSpringAnimator.getSpring().setStiffness(350.0f);
            galleryOpenCloseSpringAnimator.addEndListener((a, canceled, c, d) -> {
                if (canceled) {
                    return;
                }
                galleryListView.setTranslationY(to);
                galleryListView.ignoreScroll = false;
                galleryOpenCloseSpringAnimator = null;
                galleryListViewOpening = null;
            });
            galleryOpenCloseSpringAnimator.start();
        } else {
            galleryOpenCloseAnimator = ValueAnimator.ofFloat(from, to);
            galleryOpenCloseAnimator.addUpdateListener(anm -> {
                galleryListView.setTranslationY((float) anm.getAnimatedValue());
            });
            galleryOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    windowView.removeView(galleryListView);
                    galleryListView = null;
                    galleryOpenCloseAnimator = null;
                    galleryListViewOpening = null;
                    captionEdit.keyboardNotifier.ignore(currentPage != PAGE_PREVIEW);
                }
            });
            galleryOpenCloseAnimator.setDuration(450L);
            galleryOpenCloseAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            galleryOpenCloseAnimator.start();
        }

        if (!open && !awaitingPlayer) {
            lastGalleryScrollPosition = null;
        }

        if (!open && currentPage == PAGE_CAMERA && !noCameraPermission) {
            showCamera();
        }
    }

    private Parcelable lastGalleryScrollPosition;
    private MediaController.AlbumEntry lastGallerySelectedAlbum;
    private void createGalleryListView() {
        createGalleryListView(false);
    }

    private void createGalleryListView(boolean forAddingPart) {
        if (galleryListView != null || getContext() == null) {
            return;
        }

        galleryListView = new GalleryListView(currentAccount, getContext(), resourcesProvider, lastGallerySelectedAlbum, forAddingPart) {
            @Override
            public void setTranslationY(float translationY) {
                super.setTranslationY(translationY);
                if (applyContainerViewTranslation2) {
                    final float amplitude = windowView.getMeasuredHeight() - galleryListView.top();
                    float t = Utilities.clamp(1f - translationY / amplitude, 1, 0);
                    containerView.setTranslationY2(t * dp(-32));
                    containerView.setAlpha(1 - .6f * t);
                    actionBarContainer.setAlpha(1f - t);
                }
            }

            @Override
            public void firstLayout() {
                galleryListView.setTranslationY(windowView.getMeasuredHeight() - galleryListView.top());
                if (galleryLayouted != null) {
                    galleryLayouted.run();
                    galleryLayouted = null;
                }
            }

            @Override
            protected void onFullScreen(boolean isFullscreen) {
                if (currentPage == PAGE_CAMERA && isFullscreen) {
                    AndroidUtilities.runOnUIThread(() -> {
//                        destroyCameraView(true);
                        cameraViewThumb.setImageDrawable(getCameraThumb());
                    });
                }
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getY() < top()) {
                    galleryClosing = true;
                    animateGalleryListView(false);
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }
        };
        galleryListView.allowSearch(false);
        galleryListView.setOnBackClickListener(() -> {
            animateGalleryListView(false);
            lastGallerySelectedAlbum = null;
        });
        galleryListView.setOnSelectListener((entry, blurredBitmap) -> {
            if (entry == null || galleryListViewOpening != null || scrollingY || !isGalleryOpen()) {
                return;
            }

            if (forAddingPart) {
                if (outputEntry == null) {
                    return;
                }
                createPhotoPaintView();
                outputEntry.editedMedia = true;
                if (entry instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry;
                    paintView.appearAnimation(paintView.createPhoto(photoEntry.path, false));
                } else if (entry instanceof TLObject) {
                    paintView.appearAnimation(paintView.createPhoto((TLObject) entry, false));
                }
                animateGalleryListView(false);
            } else {
                StoryEntry storyEntry;
                showVideoTimer(false, true);
                modeSwitcherView.switchMode(isVideo);
                recordControl.startAsVideo(isVideo);
                recordControl.startAsVideo(isVideo);

                animateGalleryListView(false);
                if (entry instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry;
                    isVideo = photoEntry.isVideo;
                    storyEntry = StoryEntry.fromPhotoEntry(photoEntry);
                    storyEntry.blurredVideoThumb = blurredBitmap;
                    storyEntry.botId = botId;
                    storyEntry.botLang = botLang;
                    storyEntry.setupMatrix();
                    fromGallery = true;

                    if (collageLayoutView.hasLayout()) {
                        outputFile = null;
                        storyEntry.videoVolume = 1.0f;
                        if (collageLayoutView.push(storyEntry)) {
                            outputEntry = StoryEntry.asCollage(collageLayoutView.getLayout(), collageLayoutView.getContent());
//                            StoryPrivacySelector.applySaved(currentAccount, outputEntry);
//                            navigateTo(PAGE_PREVIEW, true);
                        }
                        updateActionBarButtons(true);
                    } else {
                        outputEntry = storyEntry;
                        if (entry instanceof MediaController.PhotoEntry) {
                            StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                        }
                        navigateTo(PAGE_PREVIEW, true);
                    }
                } else if (entry instanceof StoryEntry) {
                    storyEntry = (StoryEntry) entry;
                    if (storyEntry.file == null && !storyEntry.isCollage()) {
                        downloadButton.showToast(R.raw.error, "Failed to load draft");
                        MessagesController.getInstance(currentAccount).getStoriesController().getDraftsController().delete(storyEntry);
                        return;
                    }
                    storyEntry.botId = botId;
                    storyEntry.botLang = botLang;
                    storyEntry.setupMatrix();
                    isVideo = storyEntry.isVideo;
                    storyEntry.blurredVideoThumb = blurredBitmap;
                    fromGallery = false;

                    collageLayoutView.set(storyEntry, true);
                    outputEntry = storyEntry;
                    if (entry instanceof MediaController.PhotoEntry) {
                        StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                    }
                    navigateTo(PAGE_PREVIEW, true);
                } else {
                    return;
                }
            }

            if (galleryListView != null) {
                lastGalleryScrollPosition = galleryListView.layoutManager.onSaveInstanceState();
                lastGallerySelectedAlbum = galleryListView.getSelectedAlbum();
            }
        });
        if (lastGalleryScrollPosition != null) {
            galleryListView.layoutManager.onRestoreInstanceState(lastGalleryScrollPosition);
        }
        windowView.addView(galleryListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));
    }

    private boolean isGalleryOpen() {
        return !scrollingY && galleryListView != null && galleryListView.getTranslationY() < (windowView.getMeasuredHeight() - (int) (AndroidUtilities.displaySize.y * 0.35f) - (AndroidUtilities.statusBarHeight + ActionBar.getCurrentActionBarHeight()));
    }

    private void onNavigateStart(int fromPage, int toPage) {
        if (toPage == PAGE_CAMERA) {
            requestCameraPermission(false);
            recordControl.setVisibility(View.VISIBLE);
            if (recordControl != null) {
                recordControl.stopRecordingLoading(false);
            }
            modeSwitcherView.setVisibility(View.VISIBLE);
            zoomControlView.setVisibility(View.VISIBLE);
            zoomControlView.setAlpha(0);
            videoTimerView.setDuration(0, true);

            if (outputEntry != null) {
                outputEntry.destroy(false);
                outputEntry = null;
            }
            if (collageLayoutView != null) {
                collageLayoutView.clear(true);
                recordControl.setCollageProgress(0.0f, false);
            }
        }
        if (fromPage == PAGE_CAMERA) {
            setCameraFlashModeIcon(null, true);
            saveLastCameraBitmap(() -> cameraViewThumb.setImageDrawable(getCameraThumb()));
            if (draftSavedHint != null) {
                draftSavedHint.setVisibility(View.GONE);
            }
            cameraHint.hide();
            if (dualHint != null) {
                dualHint.hide();
            }
        }
        if (toPage == PAGE_PREVIEW || fromPage == PAGE_PREVIEW) {
            downloadButton.setEntry(toPage == PAGE_PREVIEW ? outputEntry : null);
            if (isVideo) {
                muteButton.setVisibility(View.VISIBLE);
                setIconMuted(outputEntry != null && outputEntry.muted, false);
                playButton.setVisibility(View.VISIBLE);
                previewView.play(true);
                playButton.drawable.setPause(previewView.isPlaying(), false);
                titleTextView.setRightPadding(AndroidUtilities.dp(144));
            } else if (outputEntry != null && !TextUtils.isEmpty(outputEntry.audioPath)) {
                muteButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                playButton.drawable.setPause(true, false);
                titleTextView.setRightPadding(AndroidUtilities.dp(48));
            } else {
                titleTextView.setRightPadding(AndroidUtilities.dp(48));
            }
            downloadButton.setVisibility(View.VISIBLE);
            if (outputEntry != null && outputEntry.isRepostMessage) {
                getThemeButton().setVisibility(View.VISIBLE);
                updateThemeButtonDrawable(false);
            } else if (themeButton != null) {
                themeButton.setVisibility(View.GONE);
            }
//            privacySelector.setVisibility(View.VISIBLE);
            previewButtons.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.VISIBLE);
            captionEdit.setVisibility(isBot() ? View.GONE : View.VISIBLE);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) videoTimelineContainerView.getLayoutParams();
            lp.bottomMargin = isBot() ? dp(12) : dp(68);
            videoTimelineContainerView.setLayoutParams(lp);
            captionContainer.setVisibility(View.VISIBLE);
            captionContainer.clearFocus();

//            privacySelector.setStoryPeriod(outputEntry == null || !UserConfig.getInstance(currentAccount).isPremium() ? 86400 : outputEntry.period);
            captionEdit.setPeriod(outputEntry == null ? 86400 : outputEntry.period, false);
            captionEdit.setPeriodVisible(!MessagesController.getInstance(currentAccount).premiumFeaturesBlocked() && (outputEntry == null || !outputEntry.isEdit));
            captionEdit.setHasRoundVideo(outputEntry != null && outputEntry.round != null);
            setReply();
            timelineView.setOpen(outputEntry == null || !outputEntry.isCollage() || !outputEntry.hasVideo(), false);
        }
        if (toPage == PAGE_COVER || fromPage == PAGE_COVER) {
            titleTextView.setVisibility(View.VISIBLE);
            coverTimelineView.setVisibility(View.VISIBLE);
            if (outputEntry != null && outputEntry.isEditingCover) {
                titleTextView.setText(getString(R.string.RecorderEditCover));
            }
            captionContainer.setVisibility(View.VISIBLE);
            coverButton.setVisibility(View.VISIBLE);
        }
        if (toPage == PAGE_COVER) {
            titleTextView.setText(getString(R.string.RecorderEditCover));
        }
        if (toPage == PAGE_PREVIEW) {
            videoError = false;
            final boolean isBot = outputEntry != null && outputEntry.botId != 0;
            final boolean isEdit = outputEntry != null && outputEntry.isEdit;
            previewButtons.setShareText(getString(isEdit ? R.string.Done : isBot ? R.string.UploadBotPreview : R.string.Next), !isBot);
            coverTimelineView.setVisibility(View.GONE);
            coverButton.setVisibility(View.GONE);
//            privacySelector.set(outputEntry, false);
            if (!previewAlreadySet) {
                if (outputEntry != null && outputEntry.isRepostMessage) {
                    previewView.preset(outputEntry);
                } else {
                    previewView.set(outputEntry);
                }
            }
            previewAlreadySet = false;
            captionEdit.editText.getEditText().setOnPremiumMenuLockClickListener(MessagesController.getInstance(currentAccount).storyEntitiesAllowed() ? null : () -> {
                BulletinFactory.of(windowView, resourcesProvider).createSimpleBulletin(R.raw.voip_invite, premiumText(getString(R.string.StoryPremiumFormatting))).show(true);
            });
            if (outputEntry != null && (outputEntry.isDraft || outputEntry.isEdit)) {
                if (outputEntry.paintFile != null) {
                    destroyPhotoPaintView();
                    createPhotoPaintView();
                    hidePhotoPaintView();
                }
//                if (outputEntry.filterState != null) {
//                    destroyPhotoFilterView();
//                    createFilterPhotoView();
//                }
                if (outputEntry.isVideo && outputEntry.filterState != null) {
                    VideoEditTextureView textureView = previewView.getTextureView();
                    if (textureView != null) {
                        textureView.setDelegate(eglThread -> {
                            if (eglThread != null && outputEntry != null && outputEntry.filterState != null) {
                                eglThread.setFilterGLThreadDelegate(FilterShaders.getFilterShadersDelegate(outputEntry.filterState));
                            }
                        });
                    }
                }
                captionEdit.setText(outputEntry.caption);
            } else {
                captionEdit.clear();
            }
            previewButtons.setFiltersVisible(outputEntry == null || (!outputEntry.isRepostMessage || outputEntry.isVideo) && !outputEntry.isCollage());
            previewButtons.setShareEnabled(!videoError && !captionEdit.isCaptionOverLimit() && (!MessagesController.getInstance(currentAccount).getStoriesController().hasStoryLimit() || (outputEntry != null && (outputEntry.isEdit || outputEntry.botId != 0))));
            muteButton.setImageResource(outputEntry != null && outputEntry.muted ? R.drawable.media_unmute : R.drawable.media_mute);
            previewView.setVisibility(View.VISIBLE);
            timelineView.setVisibility(View.VISIBLE);
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setTranslationX(0);
            if (outputEntry != null && outputEntry.botId != 0) {
                titleTextView.setText("");
            } else if (outputEntry != null && outputEntry.isEdit) {
                titleTextView.setText(getString(R.string.RecorderEditStory));
            } else if (outputEntry != null && outputEntry.isRepostMessage) {
                titleTextView.setText(getString(R.string.RecorderRepost));
            } else if (outputEntry != null && outputEntry.isRepost) {
                SpannableStringBuilder title = new SpannableStringBuilder();
                AvatarSpan span = new AvatarSpan(titleTextView, currentAccount, 32);
                titleTextView.setTranslationX(-dp(6));
                SpannableString avatar = new SpannableString("a");
                avatar.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (outputEntry.repostPeer instanceof TLRPC.TL_peerUser) {
                    TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(outputEntry.repostPeer.user_id);
                    span.setUser(user);
                    title.append(avatar).append("  ");
                    title.append(UserObject.getUserName(user));
                } else {
                    TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-DialogObject.getPeerDialogId(outputEntry.repostPeer));
                    span.setChat(chat);
                    title.append(avatar).append("  ");
                    title.append(chat != null ? chat.title : "");
                }
                titleTextView.setText(title);
            } else {
                titleTextView.setText(getString(R.string.RecorderNewStory));
            }

//            MediaDataController.getInstance(currentAccount).checkStickers(MediaDataController.TYPE_EMOJIPACKS);
//            MediaDataController.getInstance(currentAccount).checkFeaturedEmoji();
        }
        if (fromPage == PAGE_PREVIEW) {
//            privacySelectorHint.hide();
            captionEdit.hidePeriodPopup();
            muteHint.hide();
        }
        if (toPage == PAGE_COVER) {
            if (outputEntry != null) {
                if (outputEntry.cover < 0) {
                    outputEntry.cover = 0;
                }
                coverValue = outputEntry.cover;
                long duration = previewView.getDuration() < 100 ? outputEntry.duration : previewView.getDuration();
                if (outputEntry.duration <= 0) {
                    outputEntry.duration = duration;
                }
                coverTimelineView.setVideo(false, outputEntry.getOriginalFile().getAbsolutePath(), outputEntry.duration, outputEntry.videoVolume);
                coverTimelineView.setCoverVideo((long) (outputEntry.left * duration), (long) (outputEntry.right * duration));
                final Utilities.Callback2<Boolean, Float> videoLeftSet = (start, left) -> {
                    final long _duration = previewView.getDuration() < 100 ? outputEntry.duration : previewView.getDuration();
                    coverValue = (long) ((left + 0.04f * (left / (1f - 0.04f))) * (outputEntry.right - outputEntry.left) * _duration);
                    previewView.seekTo(coverValue = (long) (outputEntry.left * _duration + coverValue), false);
                    if (paintView != null) {
                        paintView.setCoverTime(coverValue);
                    }
                    if (outputEntry != null && outputEntry.isEdit) {
                        outputEntry.editedMedia = true;
                    }
                };
                coverTimelineView.setDelegate(new TimelineView.TimelineDelegate() {
                    @Override
                    public void onVideoLeftChange(float left) {
                        videoLeftSet.run(false, left);
                    }
                });
                float left = (float) coverValue / Math.max(1, duration) * (1f - 0.04f);
                coverTimelineView.setVideoLeft(left);
                coverTimelineView.setVideoRight(left + 0.04f);
                videoLeftSet.run(true, left);
            }
        }
        if (photoFilterEnhanceView != null) {
            photoFilterEnhanceView.setAllowTouch(false);
        }
        cameraViewThumb.setClickable(false);
        if (savedDualHint != null) {
            savedDualHint.hide();
        }
        Bulletin.hideVisible();

        if (captionEdit != null) {
            captionEdit.closeKeyboard();
            captionEdit.ignoreTouches = true;
        }
        if (previewView != null) {
            previewView.updatePauseReason(8, toPage != PAGE_PREVIEW);
        }
        if (paintView != null) {
            paintView.setCoverPreview(toPage != PAGE_PREVIEW);
        }
        if (removeCollageHint != null) {
            removeCollageHint.hide();
        }
        collageLayoutView.setPreview(toPage == PAGE_PREVIEW && collageLayoutView.hasLayout());
    }

    private void onNavigateEnd(int fromPage, int toPage) {
        if (fromPage == PAGE_CAMERA) {
//            destroyCameraView(false);
            recordControl.setVisibility(View.GONE);
            zoomControlView.setVisibility(View.GONE);
            modeSwitcherView.setVisibility(View.GONE);
//            dualButton.setVisibility(View.GONE);
            animateRecording(false, false);
            setAwakeLock(false);
        }
        cameraViewThumb.setClickable(toPage == PAGE_CAMERA);
        if (fromPage == PAGE_COVER) {
            coverTimelineView.setVisibility(View.GONE);
            captionContainer.setVisibility(toPage == PAGE_PREVIEW ? View.VISIBLE : View.GONE);
            captionEdit.setVisibility(View.GONE);
            coverButton.setVisibility(View.GONE);
        }
        if (fromPage == PAGE_PREVIEW) {
            previewButtons.setVisibility(View.GONE);
            captionContainer.setVisibility(toPage == PAGE_COVER ? View.VISIBLE : View.GONE);
            muteButton.setVisibility(View.GONE);
            playButton.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);
            if (themeButton != null) {
                themeButton.setVisibility(View.GONE);
            }
//            privacySelector.setVisibility(View.GONE);
            previewView.setVisibility(toPage == PAGE_COVER ? View.VISIBLE : View.GONE);
            timelineView.setVisibility(View.GONE);
            if (toPage != PAGE_COVER) {
                destroyPhotoPaintView();
                destroyPhotoFilterView();
            }
            titleTextView.setVisibility(toPage == PAGE_COVER ? View.VISIBLE : View.GONE);
            destroyGalleryListView();
            trash.setAlpha(0f);
            trash.setVisibility(View.GONE);
            videoTimeView.setVisibility(View.GONE);
        }
        if (toPage == PAGE_PREVIEW) {
            if (outputEntry == null || !outputEntry.isRepost) {
                createPhotoPaintView();
                hidePhotoPaintView();
            }
            if (outputEntry == null || !outputEntry.isRepost && !outputEntry.isRepostMessage) {
                createFilterPhotoView();
            }
            if (photoFilterEnhanceView != null) {
                photoFilterEnhanceView.setAllowTouch(false);
            }
            previewView.updatePauseReason(2, false);
            previewView.updatePauseReason(3, false);
            previewView.updatePauseReason(4, false);
            previewView.updatePauseReason(5, false);
            previewView.updatePauseReason(7, false);
            videoTimeView.setVisibility(outputEntry != null && outputEntry.duration >= 30_000 ? View.VISIBLE : View.GONE);
            captionContainer.setAlpha(1f);
            captionContainer.setTranslationY(0);
            captionEdit.setVisibility(outputEntry != null && outputEntry.botId != 0 ? View.GONE : View.VISIBLE);
        }
        if (toPage == PAGE_CAMERA && showSavedDraftHint) {
            getDraftSavedHint().setVisibility(View.VISIBLE);
            getDraftSavedHint().show();
            recordControl.updateGalleryImage();
        }
        showSavedDraftHint = false;

        if (photoFilterEnhanceView != null) {
            photoFilterEnhanceView.setAllowTouch(toPage == PAGE_PREVIEW && (currentEditMode == EDIT_MODE_NONE || currentEditMode == EDIT_MODE_FILTER));
        }
//        if (toPage == PAGE_PREVIEW && !privacySelectorHintOpened) {
//            privacySelectorHint.show(false);
//            privacySelectorHintOpened = true;
//        }
        if (captionEdit != null) {
            captionEdit.ignoreTouches = toPage != PAGE_PREVIEW;
        }
        if (toPage == PAGE_CAMERA) {
            cameraView.resetCamera();
        }

        if (toPage == PAGE_PREVIEW) {
            MediaDataController.getInstance(currentAccount).checkStickers(MediaDataController.TYPE_IMAGE);
            MediaDataController.getInstance(currentAccount).loadRecents(MediaDataController.TYPE_IMAGE, false, true, false);
            MediaDataController.getInstance(currentAccount).loadRecents(MediaDataController.TYPE_FAVE, false, true, false);
            MessagesController.getInstance(currentAccount).getStoriesController().loadBlocklistAtFirst();
            MessagesController.getInstance(currentAccount).getStoriesController().loadSendAs();
        }
    }

    private void setReply() {
        if (captionEdit == null) return;
        if (outputEntry == null || !outputEntry.isRepost) {
            captionEdit.setReply(null, null);
        } else {
            TLRPC.Peer peer = outputEntry.repostPeer;
            CharSequence peerName;
            if (peer instanceof TLRPC.TL_peerUser) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(peer.user_id);
                String name = UserObject.getUserName(user);
                peerName = outputEntry.repostPeerName = new SpannableStringBuilder(MessageObject.userSpan()).append(" ").append(name);
            } else {
                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-DialogObject.getPeerDialogId(peer));
                String name = chat == null ? "" : chat.title;
                peerName = outputEntry.repostPeerName = new SpannableStringBuilder(MessageObject.userSpan()).append(" ").append(name);
            }
            CharSequence repostCaption = outputEntry.repostCaption;
            if (TextUtils.isEmpty(repostCaption)) {
                SpannableString s = new SpannableString(getString(R.string.Story));
                s.setSpan(new CharacterStyle() {
                    @Override
                    public void updateDrawState(TextPaint tp) {
                        tp.setAlpha(0x80);
                    }
                }, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                repostCaption = s;
            }
            captionEdit.setReply(peerName, repostCaption);
        }
    }

    private boolean isBot() {
        return outputEntry != null && outputEntry.botId != 0 || botId != 0;
    }

    private void onSwitchEditModeStart(int fromMode, int toMode) {
        if (toMode == EDIT_MODE_NONE) {
            backButton.setVisibility(View.VISIBLE);
            captionEdit.setVisibility(View.VISIBLE);
            if (paintView != null) {
                paintView.clearSelection();
            }
            downloadButton.setVisibility(View.VISIBLE);
            if (outputEntry != null && outputEntry.isRepostMessage) {
                getThemeButton().setVisibility(View.VISIBLE);
                updateThemeButtonDrawable(false);
            } else if (themeButton != null) {
                themeButton.setVisibility(View.GONE);
            }
            titleTextView.setVisibility(View.VISIBLE);
//            privacySelector.setVisibility(View.VISIBLE);
            if (isVideo) {
                muteButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
            } else if (outputEntry != null && !TextUtils.isEmpty(outputEntry.audioPath)) {
                muteButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }
            timelineView.setVisibility(View.VISIBLE);
        }
        if (toMode == EDIT_MODE_PAINT && paintView != null) {
            paintView.setVisibility(View.VISIBLE);
        }
        if ((toMode == EDIT_MODE_PAINT || fromMode == EDIT_MODE_PAINT) && paintView != null) {
            paintView.onAnimationStateChanged(true);
        }

        if (paintView != null) {
            paintView.keyboardNotifier.ignore(toMode != EDIT_MODE_PAINT);
        }
        captionEdit.keyboardNotifier.ignore(toMode != EDIT_MODE_NONE);
//        privacySelectorHint.hide();
        Bulletin.hideVisible();
        if (photoFilterView != null && fromMode == EDIT_MODE_FILTER) {
            applyFilter(null);
        }
        if (photoFilterEnhanceView != null) {
            photoFilterEnhanceView.setAllowTouch(false);
        }
        muteHint.hide();
    }

    private void onSwitchEditModeEnd(int fromMode, int toMode) {
        if (toMode == EDIT_MODE_PAINT) {
            backButton.setVisibility(View.GONE);
        }
        if (fromMode == EDIT_MODE_PAINT && paintView != null) {
            paintView.setVisibility(View.GONE);
        }
        if (fromMode == EDIT_MODE_NONE) {
            captionEdit.setVisibility(View.GONE);
            muteButton.setVisibility(toMode == EDIT_MODE_TIMELINE ? View.VISIBLE : View.GONE);
            playButton.setVisibility(toMode == EDIT_MODE_TIMELINE ? View.VISIBLE : View.GONE);
            downloadButton.setVisibility(toMode == EDIT_MODE_TIMELINE ? View.VISIBLE : View.GONE);
            if (themeButton != null) {
                themeButton.setVisibility(toMode == EDIT_MODE_TIMELINE ? View.VISIBLE : View.GONE);
            }
//            privacySelector.setVisibility(View.GONE);
            timelineView.setVisibility(toMode == EDIT_MODE_TIMELINE ? View.VISIBLE : View.GONE);
            titleTextView.setVisibility(View.GONE);
        }
        previewView.setAllowCropping(toMode == EDIT_MODE_NONE);
        if ((toMode == EDIT_MODE_PAINT || fromMode == EDIT_MODE_PAINT) && paintView != null) {
            paintView.onAnimationStateChanged(false);
        }
        if (photoFilterEnhanceView != null) {
            photoFilterEnhanceView.setAllowTouch(toMode == EDIT_MODE_FILTER || toMode == EDIT_MODE_NONE);
        }
    }

    private void destroyCameraView(boolean waitForThumb) {
        if (cameraView != null) {
            if (waitForThumb) {
                saveLastCameraBitmap(() -> {
                    cameraViewThumb.setImageDrawable(getCameraThumb());
                    if (cameraView != null) {
                        cameraView.destroy(true, null);
                        AndroidUtilities.removeFromParent(cameraView);
                        if (collageLayoutView != null) {
                            collageLayoutView.setCameraView(null);
                        }
                        cameraView = null;
                    }
                });
            } else {
                saveLastCameraBitmap(() -> {
                    cameraViewThumb.setImageDrawable(getCameraThumb());
                });
                cameraView.destroy(true, null);
                AndroidUtilities.removeFromParent(cameraView);
                if (collageLayoutView != null) {
                    collageLayoutView.setCameraView(null);
                }
                cameraView = null;
            }
        }
    }

    private View changeDayNightView;
    private ValueAnimator changeDayNightViewAnimator;
    private float changeDayNightViewProgress;
    public ImageView getThemeButton() {
        if (themeButton == null) {
            themeButtonDrawable = new RLottieDrawable(R.raw.sun_outline, "" + R.raw.sun_outline, dp(28), dp(28), true, null);
            themeButtonDrawable.setPlayInDirectionOfCustomEndFrame(true);
            if (!(outputEntry != null && outputEntry.isDark)) {
                themeButtonDrawable.setCustomEndFrame(0);
                themeButtonDrawable.setCurrentFrame(0);
            } else {
                themeButtonDrawable.setCurrentFrame(35);
                themeButtonDrawable.setCustomEndFrame(36);
            }
            themeButtonDrawable.beginApplyLayerColors();
            int color = Theme.getColor(Theme.key_chats_menuName, resourcesProvider);
            themeButtonDrawable.setLayerColor("Sunny.**", color);
            themeButtonDrawable.setLayerColor("Path 6.**", color);
            themeButtonDrawable.setLayerColor("Path.**", color);
            themeButtonDrawable.setLayerColor("Path 5.**", color);
            themeButtonDrawable.commitApplyLayerColors();
            themeButton = new ImageView(getContext());
            themeButton.setScaleType(ImageView.ScaleType.CENTER);
            themeButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
            themeButton.setBackground(Theme.createSelectorDrawable(0x20ffffff));
            themeButton.setOnClickListener(e -> {
                toggleTheme();
            });
//            themeButton.setOnLongClickListener(e -> {
//                openThemeSheet();
//                return true;
//            });
            themeButton.setVisibility(View.GONE);
            themeButton.setImageDrawable(themeButtonDrawable);
            themeButton.setAlpha(0f);
            actionBarButtons.addView(themeButton, 0, LayoutHelper.createLinear(46, 56, Gravity.TOP | Gravity.RIGHT));
        }
        return themeButton;
    }

    public void updateThemeButtonDrawable(boolean animated) {
        if (themeButtonDrawable != null) {
            if (animated) {
                themeButtonDrawable.setCustomEndFrame(outputEntry != null && outputEntry.isDark ? themeButtonDrawable.getFramesCount() : 0);
                if (themeButtonDrawable != null) {
                    themeButtonDrawable.start();
                }
            } else {
                int frame = outputEntry != null && outputEntry.isDark ? themeButtonDrawable.getFramesCount() - 1 : 0;
                themeButtonDrawable.setCurrentFrame(frame, false, true);
                themeButtonDrawable.setCustomEndFrame(frame);
                if (themeButton != null) {
                    themeButton.invalidate();
                }
            }
        }
    }

    public void toggleTheme() {
        if (outputEntry == null || changeDayNightView != null || themeButton == null || changeDayNightViewAnimator != null && changeDayNightViewAnimator.isRunning()) {
            return;
        }
        final boolean isDark = outputEntry.isDark;

        Bitmap bitmap = Bitmap.createBitmap(windowView.getWidth(), windowView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas bitmapCanvas = new Canvas(bitmap);
        themeButton.setAlpha(0f);
        if (previewView != null) {
            previewView.drawForThemeToggle = true;
        }
        if (paintView != null) {
            paintView.drawForThemeToggle = true;
        }
        windowView.draw(bitmapCanvas);
        if (previewView != null) {
            previewView.drawForThemeToggle = false;
        }
        if (paintView != null) {
            paintView.drawForThemeToggle = false;
        }
        themeButton.setAlpha(1f);

        Paint xRefPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xRefPaint.setColor(0xff000000);
        xRefPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setFilterBitmap(true);
        int[] position = new int[2];
        themeButton.getLocationInWindow(position);
        float x = position[0];
        float y = position[1];
        float cx = x + themeButton.getMeasuredWidth() / 2f;
        float cy = y + themeButton.getMeasuredHeight() / 2f;

        float r = Math.max(bitmap.getHeight(), bitmap.getWidth()) + AndroidUtilities.navigationBarHeight;

        Shader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapPaint.setShader(bitmapShader);
        changeDayNightView = new View(getContext()) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (isDark) {
                    if (changeDayNightViewProgress > 0f) {
                        bitmapCanvas.drawCircle(cx, cy, r * changeDayNightViewProgress, xRefPaint);
                    }
                    canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
                } else {
                    canvas.drawCircle(cx, cy, r * (1f - changeDayNightViewProgress), bitmapPaint);
                }
                canvas.save();
                canvas.translate(x, y);
                themeButton.draw(canvas);
                canvas.restore();
            }
        };
        changeDayNightView.setOnTouchListener((v, event) -> true);
        changeDayNightViewProgress = 0f;
        changeDayNightViewAnimator = ValueAnimator.ofFloat(0, 1f);
        changeDayNightViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean changedNavigationBarColor = false;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                changeDayNightViewProgress = (float) valueAnimator.getAnimatedValue();
                if (changeDayNightView != null) {
                    changeDayNightView.invalidate();
                }
                if (!changedNavigationBarColor && changeDayNightViewProgress > .5f) {
                    changedNavigationBarColor = true;
                }
            }
        });
        changeDayNightViewAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (changeDayNightView != null) {
                    if (changeDayNightView.getParent() != null) {
                        ((ViewGroup) changeDayNightView.getParent()).removeView(changeDayNightView);
                    }
                    changeDayNightView = null;
                }
                changeDayNightViewAnimator = null;
                super.onAnimationEnd(animation);
            }
        });
        changeDayNightViewAnimator.setStartDelay(80);
        changeDayNightViewAnimator.setDuration(isDark ? 320 : 450);
        changeDayNightViewAnimator.setInterpolator(isDark ? CubicBezierInterpolator.EASE_IN : CubicBezierInterpolator.EASE_OUT_QUINT);
        changeDayNightViewAnimator.start();

        windowView.addView(changeDayNightView, new ViewGroup.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        AndroidUtilities.runOnUIThread(() -> {
            if (outputEntry == null) {
                return;
            }
            outputEntry.isDark = !outputEntry.isDark;
            if (previewView != null) {
                previewView.setupWallpaper(outputEntry, false);
            }
            if (paintView != null && paintView.entitiesView != null) {
                for (int i = 0; i < paintView.entitiesView.getChildCount(); ++i) {
                    View child = paintView.entitiesView.getChildAt(i);
                    if (child instanceof MessageEntityView) {
                        ((MessageEntityView) child).setupTheme(outputEntry);
                    }
                }
            }
            updateThemeButtonDrawable(true);
        });
    }

    private void processDone() {
        if (privacySheet != null) {
            privacySheet.dismiss();
            privacySheet = null;
        }
        if (videoError) {
            downloadButton.showFailedVideo();
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            AndroidUtilities.shakeViewSpring(previewButtons.shareButton, shiftDp = -shiftDp);
            return;
        }
        if (captionEdit != null && captionEdit.isCaptionOverLimit()) {
            BotWebViewVibrationEffect.APP_ERROR.vibrate();
            AndroidUtilities.shakeViewSpring(captionEdit.limitTextView, shiftDp = -shiftDp);
            captionEdit.captionLimitToast();
            return;
        }
        outputEntry.captionEntitiesAllowed = MessagesController.getInstance(currentAccount).storyEntitiesAllowed();
        if (captionEdit != null && !outputEntry.captionEntitiesAllowed) {
            CharSequence text = captionEdit.getText();
            if (text instanceof Spannable && (
                    ((Spannable) text).getSpans(0, text.length(), TextStyleSpan.class).length > 0 ||
                            ((Spannable) text).getSpans(0, text.length(), URLSpan.class).length > 0
            )) {
                BulletinFactory.of(windowView, resourcesProvider).createSimpleBulletin(R.raw.voip_invite, premiumText(getString(R.string.StoryPremiumFormatting))).show(true);
                AndroidUtilities.shakeViewSpring(captionEdit, shiftDp = -shiftDp);
                return;
            }
        }
        if (outputEntry.isEdit || outputEntry.botId != 0) {
            outputEntry.editedPrivacy = false;
            applyFilter(null);
        } else {
            previewView.updatePauseReason(3, true);
            if (outputEntry.isVideo) {
                if (previewView != null && !outputEntry.coverSet && currentPage != PAGE_COVER) {
                    outputEntry.cover = previewView.getCurrentPosition();
                    previewView.getCoverBitmap(bitmap -> {
                        if (outputEntry == null) return;
                        if (outputEntry.coverBitmap != null) {
                            outputEntry.coverBitmap.recycle();
                        }
                        outputEntry.coverBitmap = bitmap;
                        if (privacySheet == null) return;
                        privacySheet.setCover(outputEntry.coverBitmap);
                    }, previewView, paintViewRenderView, paintViewEntitiesView);
                }
                privacySheet.setCover(outputEntry.coverBitmap, () -> {
                    if (privacySheet != null) {
                        privacySheet.dismiss();
                    }
                    navigateTo(PAGE_COVER, true);
                });
            }
            privacySheet.setOnDismissListener(di -> {
                previewView.updatePauseReason(3, false);
                privacySheet = null;
            });
            privacySheet.show();
        }
    }

    private Bitmap getUiBlurBitmap() {
        Bitmap blur = null;
        if (photoFilterView != null) {
            blur = photoFilterView.getUiBlurBitmap();
        }
        if (blur == null && previewView != null && previewView.getTextureView() != null) {
            blur = previewView.getTextureView().getUiBlurBitmap();
        }
        return blur;
    }

    private final RecordControl.Delegate recordControlDelegate = new RecordControl.Delegate() {
        @Override
        public boolean canRecordAudio() {
            return requestAudioPermission();
        }

        @Override
        public void onPhotoShoot() {
            if (takingPhoto || awaitingPlayer || currentPage != PAGE_CAMERA || cameraView == null || !cameraView.isInited()) {
                return;
            }
            cameraHint.hide();
            if (outputFile != null) {
                try {
                    outputFile.delete();
                } catch (Exception ignore) {}
                outputFile = null;
            }
            outputFile = StoryEntry.makeCacheFile(currentAccount, false);
            takingPhoto = true;
            checkFrontfaceFlashModes();
            isDark = false;
            if (cameraView.isFrontface() && frontfaceFlashMode == 1) {
                checkIsDark();
            }
            if (useDisplayFlashlight()) {
                flashViews.flash(this::takePicture);
            } else {
                takePicture(null);
            }
        }

        @Override
        public void onCheckClick() {
            ArrayList<StoryEntry> entries = collageLayoutView.getContent();
            if (entries.size() == 1) {
                outputEntry = entries.get(0);
            } else {
                outputEntry = StoryEntry.asCollage(collageLayoutView.getLayout(), collageLayoutView.getContent());
            }
            isVideo = outputEntry != null && outputEntry.isVideo;
            if (modeSwitcherView != null) {
                modeSwitcherView.switchMode(isVideo);
            }
            StoryPrivacySelector.applySaved(currentAccount, outputEntry);
            navigateTo(PAGE_PREVIEW, true);
        }

        private void takePicture(Utilities.Callback<Runnable> done) {
            boolean savedFromTextureView = false;
            if (!useDisplayFlashlight()) {
                cameraView.startTakePictureAnimation(true);
            }
            if (cameraView.isDual() && TextUtils.equals(cameraView.getCameraSession().getCurrentFlashMode(), Camera.Parameters.FLASH_MODE_OFF) || collageLayoutView.hasLayout()) {
                if (!collageLayoutView.hasLayout()) {
                    cameraView.pauseAsTakingPicture();
                }
                final Bitmap bitmap = cameraView.getTextureView().getBitmap();
                try (FileOutputStream out = new FileOutputStream(outputFile.getAbsoluteFile())) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    savedFromTextureView = true;
                } catch (Exception e) {
                    FileLog.e(e);
                }
                bitmap.recycle();
            }
            if (!savedFromTextureView) {
                takingPhoto = CameraController.getInstance().takePicture(outputFile, true, cameraView.getCameraSessionObject(), (orientation) -> {
                    if (useDisplayFlashlight()) {
                        try {
                            windowView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                        } catch (Exception ignore) {}
                    }
                    takingPhoto = false;
                    if (outputFile == null) {
                        return;
                    }
                    int w = -1, h = -1;
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(outputFile.getAbsolutePath(), opts);
                        w = opts.outWidth;
                        h = opts.outHeight;
                    } catch (Exception ignore) {}

                    int rotate = orientation == -1 ? 0 : 90;
                    if (orientation == -1) {
                        if (w > h) {
                            rotate = 270;
                        }
                    } else if (h > w && rotate != 0) {
                        rotate = 0;
                    }
                    StoryEntry entry = StoryEntry.fromPhotoShoot(outputFile, rotate);
                    if (entry != null) {
                        entry.botId = botId;
                        entry.botLang = botLang;
                    }
                    if (collageLayoutView.hasLayout()) {
                        outputFile = null;
                        if (collageLayoutView.push(entry)) {
                            outputEntry = StoryEntry.asCollage(collageLayoutView.getLayout(), collageLayoutView.getContent());
                            StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                            fromGallery = false;

                            if (done != null) {
                                done.run(null);
                            }
//                            if (done != null) {
//                                done.run(() -> navigateTo(PAGE_PREVIEW, true));
//                            } else {
//                                navigateTo(PAGE_PREVIEW, true);
//                            }
                        } else if (done != null) {
                            done.run(null);
                        }
                        updateActionBarButtons(true);
                    } else {
                        outputEntry = entry;
                        StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                        fromGallery = false;

                        if (done != null) {
                            done.run(() -> navigateTo(PAGE_PREVIEW, true));
                        } else {
                            navigateTo(PAGE_PREVIEW, true);
                        }
                    }
                });
            } else {
                takingPhoto = false;
                final StoryEntry entry = StoryEntry.fromPhotoShoot(outputFile, 0);
                entry.botId = botId;
                entry.botLang = botLang;
                if (collageLayoutView.hasLayout()) {
                    outputFile = null;
                    if (collageLayoutView.push(entry)) {
                        outputEntry = StoryEntry.asCollage(collageLayoutView.getLayout(), collageLayoutView.getContent());
                        StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                        fromGallery = false;
                        if (done != null) {
                            done.run(null);
                        }
//                        if (done != null) {
//                            done.run(() -> navigateTo(PAGE_PREVIEW, true));
//                        } else {
//                            navigateTo(PAGE_PREVIEW, true);
//                        }
                    } else if (done != null) {
                        done.run(null);
                    }
                    updateActionBarButtons(true);
                } else {
                    outputEntry = entry;
                    StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                    fromGallery = false;

                    if (done != null) {
                        done.run(() -> navigateTo(PAGE_PREVIEW, true));
                    } else {
                        navigateTo(PAGE_PREVIEW, true);
                    }
                }
            }
        }

        @Override
        public void onVideoRecordStart(boolean byLongPress, Runnable whenStarted) {
            if (takingVideo || stoppingTakingVideo || awaitingPlayer || currentPage != PAGE_CAMERA || cameraView == null || cameraView.getCameraSession() == null) {
                return;
            }
            if (dualHint != null) {
                dualHint.hide();
            }
            if (savedDualHint != null) {
                savedDualHint.hide();
            }
            cameraHint.hide();
            takingVideo = true;
            if (outputFile != null) {
                try {
                    outputFile.delete();
                } catch (Exception ignore) {}
                outputFile = null;
            }
            outputFile = StoryEntry.makeCacheFile(currentAccount, true);
            checkFrontfaceFlashModes();
            isDark = false;
            if (cameraView.isFrontface() && frontfaceFlashMode == 1) {
                checkIsDark();
            }
            if (useDisplayFlashlight()) {
                flashViews.flashIn(() -> startRecording(byLongPress, whenStarted));
            } else {
                startRecording(byLongPress, whenStarted);
            }
        }

        private void startRecording(boolean byLongPress, Runnable whenStarted) {
            if (cameraView == null) {
                return;
            }
            CameraController.getInstance().recordVideo(cameraView.getCameraSessionObject(), outputFile, false, (thumbPath, duration) -> {
                if (recordControl != null) {
                    recordControl.stopRecordingLoading(true);
                }
                if (useDisplayFlashlight()) {
                    flashViews.flashOut();
                }
                if (outputFile == null || cameraView == null) {
                    return;
                }

                takingVideo = false;
                stoppingTakingVideo = false;

                if (duration <= 800) {
                    animateRecording(false, true);
                    setAwakeLock(false);
                    videoTimerView.setRecording(false, true);
                    if (recordControl != null) {
                        recordControl.stopRecordingLoading(true);
                    }
                    try {
                        outputFile.delete();
                        outputFile = null;
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    if (thumbPath != null) {
                        try {
                            new File(thumbPath).delete();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    return;
                }

                showVideoTimer(false, true);

                StoryEntry entry = StoryEntry.fromVideoShoot(outputFile, thumbPath, duration);
                entry.botId = botId;
                entry.botLang = botLang;
                animateRecording(false, true);
                setAwakeLock(false);
                videoTimerView.setRecording(false, true);
                if (recordControl != null) {
                    recordControl.stopRecordingLoading(true);
                }
                if (collageLayoutView.hasLayout()) {
                    outputFile = null;
                    entry.videoVolume = 1.0f;
                    if (collageLayoutView.push(entry)) {
                        outputEntry = StoryEntry.asCollage(collageLayoutView.getLayout(), collageLayoutView.getContent());
                        StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                        fromGallery = false;
                        int width = cameraView.getVideoWidth(), height = cameraView.getVideoHeight();
                        if (width > 0 && height > 0) {
                            outputEntry.width = width;
                            outputEntry.height = height;
                            outputEntry.setupMatrix();
                        }
                    }
                    updateActionBarButtons(true);
                } else {
                    outputEntry = entry;
                    StoryPrivacySelector.applySaved(currentAccount, outputEntry);
                    fromGallery = false;
                    int width = cameraView.getVideoWidth(), height = cameraView.getVideoHeight();
                    if (width > 0 && height > 0) {
                        outputEntry.width = width;
                        outputEntry.height = height;
                        outputEntry.setupMatrix();
                    }
                    navigateToPreviewWithPlayerAwait(() -> {
                        navigateTo(PAGE_PREVIEW, true);
                    }, 0);
                }
            }, () /* onVideoStart */ -> {
                whenStarted.run();

                hintTextView.setText(getString(byLongPress ? R.string.StoryHintSwipeToZoom : R.string.StoryHintPinchToZoom), false);
                animateRecording(true, true);
                setAwakeLock(true);

                collageListView.setVisible(false, true);
                videoTimerView.setRecording(true, true);
                showVideoTimer(true, true);
            }, cameraView, true);

            if (!isVideo) {
                isVideo = true;
                collageListView.setVisible(false, true);
                showVideoTimer(isVideo, true);
                modeSwitcherView.switchMode(isVideo);
                recordControl.startAsVideo(isVideo);
            }
        }

        @Override
        public void onVideoRecordLocked() {
            hintTextView.setText(getString(R.string.StoryHintPinchToZoom), true);
        }

        @Override
        public void onVideoRecordPause() {

        }

        @Override
        public void onVideoRecordResume() {

        }

        @Override
        public void onVideoRecordEnd(boolean byDuration) {
            if (stoppingTakingVideo || !takingVideo) {
                return;
            }
            stoppingTakingVideo = true;
            AndroidUtilities.runOnUIThread(() -> {
                if (takingVideo && stoppingTakingVideo && cameraView != null) {
                    showZoomControls(false, true);
//                    animateRecording(false, true);
//                    setAwakeLock(false);
                    CameraController.getInstance().stopVideoRecording(cameraView.getCameraSessionRecording(), false, false);
                }
            }, byDuration ? 0 : 400);
        }

        @Override
        public void onVideoDuration(long duration) {
            videoTimerView.setDuration(duration, true);
        }

        @Override
        public void onGalleryClick() {
            if (currentPage == PAGE_CAMERA && !takingPhoto && !takingVideo && requestGalleryPermission()) {
                animateGalleryListView(true);
            }
        }

        private boolean requestGalleryPermission() {
            Activity activity = parentAlert.baseFragment.getParentActivity();
            if (activity != null) {
                boolean noGalleryPermission = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    noGalleryPermission = (
                            activity.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                                    activity.checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
                    );
                    if (noGalleryPermission) {
                        activity.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, 114);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    noGalleryPermission = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
                    if (noGalleryPermission) {
                        activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 114);
                    }
                }
                return !noGalleryPermission;
            }
            return true;
        }

        @Override
        public void onFlipClick() {
            if (cameraView == null || awaitingPlayer || takingPhoto || !cameraView.isInited() || currentPage != PAGE_CAMERA) {
                return;
            }
            if (savedDualHint != null) {
                savedDualHint.hide();
            }
            if (useDisplayFlashlight() && frontfaceFlashModes != null && !frontfaceFlashModes.isEmpty()) {
                final String mode = frontfaceFlashModes.get(frontfaceFlashMode);
                SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("camera", Activity.MODE_PRIVATE);
                sharedPreferences.edit().putString("flashMode", mode).commit();
            }
            cameraView.switchCamera();
            saveCameraFace(cameraView.isFrontface());
            if (useDisplayFlashlight()) {
                flashViews.flashIn(null);
            } else {
                flashViews.flashOut();
            }
        }

        @Override
        public void onFlipLongClick() {
            if (cameraView != null) {
                cameraView.toggleDual();
            }
        }

        @Override
        public void onZoom(float zoom) {
            zoomControlView.setZoom(zoom, true);
            showZoomControls(false, true);
        }
    };

    private void saveCameraFace(boolean frontface) {
        MessagesController.getGlobalMainSettings().edit().putBoolean("stories_camera", frontface).apply();
    }

    private boolean useDisplayFlashlight() {
        return (takingPhoto || takingVideo) && (cameraView != null && cameraView.isFrontface()) && (frontfaceFlashMode == 2 || frontfaceFlashMode == 1 && isDark);
    }

    private boolean isDark;
    private void checkIsDark() {
        if (cameraView == null || cameraView.getTextureView() == null) {
            isDark = false;
            return;
        }
        final Bitmap bitmap = cameraView.getTextureView().getBitmap();
        if (bitmap == null) {
            isDark = false;
            return;
        }
        float l = 0;
        final int sx = bitmap.getWidth() / 12;
        final int sy = bitmap.getHeight() / 12;
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                l += AndroidUtilities.computePerceivedBrightness(bitmap.getPixel((1 + x) * sx, (1 + y) * sy));
            }
        }
        l /= 100;
        bitmap.recycle();
        isDark = l < .22f;
    }

    private boolean videoTimerShown = true;
    private void showVideoTimer(boolean show, boolean animated) {
        if (videoTimerShown == show) {
            return;
        }

        videoTimerShown = show;
        if (animated) {
            videoTimerView.animate().alpha(show ? 1 : 0).setDuration(350).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).withEndAction(() -> {
                if (!show) {
                    videoTimerView.setRecording(false, false);
                }
            }).start();
        } else {
            videoTimerView.clearAnimation();
            videoTimerView.setAlpha(show ? 1 : 0);
            if (!show) {
                videoTimerView.setRecording(false, false);
            }
        }
    }
    private ValueAnimator containerViewBackAnimator;
    private boolean applyContainerViewTranslation2 = true;
    private void animateContainerBack() {
        if (containerViewBackAnimator != null) {
            containerViewBackAnimator.cancel();
            containerViewBackAnimator = null;
        }
        applyContainerViewTranslation2 = false;
        float y1 = containerView.getTranslationY1(), y2 = containerView.getTranslationY2(), a = containerView.getAlpha();
        containerViewBackAnimator = ValueAnimator.ofFloat(1, 0);
        containerViewBackAnimator.addUpdateListener(anm -> {
            final float t = (float) anm.getAnimatedValue();
            containerView.setTranslationY(y1 * t);
            containerView.setTranslationY2(y2 * t);
//            containerView.setAlpha(AndroidUtilities.lerp(a, 1f, t));
        });
        containerViewBackAnimator.setDuration(340);
        containerViewBackAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        containerViewBackAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                containerViewBackAnimator = null;
                containerView.setTranslationY(0);
                containerView.setTranslationY2(0);
            }
        });
        containerViewBackAnimator.start();
    }

    private void applyPaint() {
        if (paintView == null || outputEntry == null) {
            return;
        }

        outputEntry.clearPaint();
        outputEntry.editedMedia |= paintView.hasChanges();

        if (outputEntry.mediaEntities == null) {
            outputEntry.mediaEntities = new ArrayList<>();
        } else {
            outputEntry.mediaEntities.clear();
        }
        paintView.getBitmap(outputEntry.mediaEntities, outputEntry.resultWidth, outputEntry.resultHeight, false, false, false, false, outputEntry);
        if (!outputEntry.isVideo) {
            outputEntry.averageDuration = Utilities.clamp(paintView.getLcm(), 7500L, 5000L);
        }
        List<TLRPC.InputDocument> masks = paintView.getMasks();
        outputEntry.stickers = masks != null ? new ArrayList<>(masks) : null;
        final boolean isVideo = outputEntry.isVideo;
        final boolean wouldBeVideo = outputEntry.wouldBeVideo();

        outputEntry.mediaEntities = new ArrayList<>();
        Bitmap bitmap = paintView.getBitmap(outputEntry.mediaEntities, outputEntry.resultWidth, outputEntry.resultHeight, true, false, false, !isVideo, outputEntry);
        if (outputEntry.mediaEntities.isEmpty()) {
            outputEntry.mediaEntities = null;
        }

        try {
            if (outputEntry.paintFile != null) {
                outputEntry.paintFile.delete();
            }
        } catch (Exception ignore) {}
        try {
            if (outputEntry.paintEntitiesFile != null) {
                outputEntry.paintEntitiesFile.delete();
            }
        } catch (Exception ignore) {}
        try {
            if (outputEntry.paintBlurFile != null) {
                outputEntry.paintBlurFile.delete();
            }
        } catch (Exception ignore) {}
        outputEntry.paintFile = null;
        outputEntry.paintEntitiesFile = null;
        outputEntry.paintBlurFile = null;

        outputEntry.paintFile = FileLoader.getInstance(currentAccount).getPathToAttach(ImageLoader.scaleAndSaveImage(bitmap, Bitmap.CompressFormat.PNG, outputEntry.resultWidth, outputEntry.resultHeight, 87, false, 101, 101), true);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        bitmap = null;

        if (outputEntry.isRepostMessage) {
            if (outputEntry.backgroundFile != null) {
                try {
                    outputEntry.backgroundFile.delete();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                outputEntry.backgroundFile = null;
            }
            if (outputEntry.backgroundWallpaperPeerId != Long.MIN_VALUE) {
                Drawable drawable = outputEntry.backgroundDrawable;
                if (drawable == null) {
                    drawable = PreviewView.getBackgroundDrawable(null, currentAccount, outputEntry.backgroundWallpaperPeerId, isDark);
                }
                if (drawable != null) {
                    outputEntry.backgroundFile = StoryEntry.makeCacheFile(currentAccount, "webp");
                    bitmap = Bitmap.createBitmap(outputEntry.resultWidth, outputEntry.resultHeight, Bitmap.Config.ARGB_8888);
                    StoryEntry.drawBackgroundDrawable(new Canvas(bitmap), drawable, bitmap.getWidth(), bitmap.getHeight());
                    try {
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(outputEntry.backgroundFile));
                    } catch (Exception e) {
                        FileLog.e(e);
                    } finally {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        bitmap = null;
                    }
                }
            }
        }
        if (outputEntry.isRepostMessage) {
            if (outputEntry.messageVideoMaskFile != null) {
                try {
                    outputEntry.messageVideoMaskFile.delete();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                outputEntry.messageVideoMaskFile = null;
            }
            if (outputEntry.isRepostMessage && outputEntry.isVideo) {
                int videoWidth = outputEntry.width;
                int videoHeight = outputEntry.height;
                MessageEntityView messageEntityView = paintView.findMessageView();
                if (messageEntityView != null && messageEntityView.listView.getChildCount() == 1 && videoWidth > 0 && videoHeight > 0) {
                    View child = messageEntityView.listView.getChildAt(0);
                    if (child instanceof ChatMessageCell) {
                        ChatMessageCell cell = (ChatMessageCell) messageEntityView.listView.getChildAt(0);
                        ImageReceiver photoImage = cell.getPhotoImage();
                        if (photoImage != null && (int) photoImage.getImageWidth() > 0 && (int) photoImage.getImageHeight() > 0) {
                            float scale = Math.max(photoImage.getImageWidth() / videoWidth, photoImage.getImageHeight() / videoHeight);
                            final float S = 2f;
                            int w = (int) (videoWidth * scale / S), h = (int) (videoHeight * scale / S);
                            Bitmap maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                            float[] radii = new float[8];
                            for (int a = 0; a < photoImage.getRoundRadius().length; a++) {
                                radii[a * 2] = photoImage.getRoundRadius()[a];
                                radii[a * 2 + 1] = photoImage.getRoundRadius()[a];
                            }
                            Canvas canvas = new Canvas(maskBitmap);
                            Path path = new Path();
                            canvas.scale(1f / S, 1f / S);
                            AndroidUtilities.rectTmp.set(
                                    w * S / 2f - photoImage.getImageWidth() / 2f,
                                    h * S / 2f - photoImage.getImageHeight() / 2f,
                                    w * S / 2f + photoImage.getImageWidth() / 2f,
                                    h * S / 2f + photoImage.getImageHeight() / 2f
                            );
                            path.addRoundRect(AndroidUtilities.rectTmp, radii, Path.Direction.CW);
                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            paint.setColor(Color.WHITE);
                            canvas.drawPath(path, paint);
                            try {
                                outputEntry.messageVideoMaskFile = StoryEntry.makeCacheFile(currentAccount, "webp");
                                maskBitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(outputEntry.messageVideoMaskFile));
                            } catch (Exception e) {
                                FileLog.e(e);
                                outputEntry.messageVideoMaskFile = null;
                            }
                            maskBitmap.recycle();
                        }
                    }
                }
            }
        }

        if (!wouldBeVideo) {
            bitmap = paintView.getBitmap(new ArrayList<>(), outputEntry.resultWidth, outputEntry.resultHeight, false, true, false, false, outputEntry);
            outputEntry.paintEntitiesFile = FileLoader.getInstance(currentAccount).getPathToAttach(ImageLoader.scaleAndSaveImage(bitmap, Bitmap.CompressFormat.PNG, outputEntry.resultWidth, outputEntry.resultHeight, 87, false, 101, 101), true);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;
        }

        if (paintView.hasBlur()) {
            bitmap = paintView.getBlurBitmap();
            outputEntry.paintBlurFile = FileLoader.getInstance(currentAccount).getPathToAttach(ImageLoader.scaleAndSaveImage(bitmap, Bitmap.CompressFormat.PNG, outputEntry.resultWidth, outputEntry.resultHeight, 87, false, 101, 101), true);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;
        }
    }

    private void applyPaintMessage() {
        if (paintView == null || outputEntry == null) {
            return;
        }

        if (outputEntry.isRepostMessage) {
            if (outputEntry.messageFile != null) {
                try {
                    outputEntry.messageFile.delete();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                outputEntry.messageFile = null;
            }
            outputEntry.messageFile = StoryEntry.makeCacheFile(currentAccount, "webp");
            Bitmap bitmap = paintView.getBitmap(outputEntry.mediaEntities, outputEntry.resultWidth, outputEntry.resultHeight, false, false, true, !isVideo, outputEntry);
            try {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, new FileOutputStream(outputEntry.messageFile));
            } catch (Exception e) {
                FileLog.e(e);
                try {
                    outputEntry.messageFile.delete();
                } catch (Exception e2) {
                    FileLog.e(e2);
                }
                outputEntry.messageFile = null;
            } finally {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                bitmap = null;
            }
        }
    }

    private void applyFilter(Runnable whenDone) {
        if (photoFilterView == null || outputEntry == null) {
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }
        outputEntry.editedMedia |= photoFilterView.hasChanges();
        outputEntry.updateFilter(photoFilterView, whenDone);
        if (whenDone == null && !outputEntry.isVideo && previewView != null) {
            previewView.set(outputEntry);
        }
    }

    private void saveFrontFaceFlashMode() {
        if (frontfaceFlashMode >= 0) {
            MessagesController.getGlobalMainSettings().edit()
                    .putFloat("frontflash_warmth", flashViews.warmth)
                    .putFloat("frontflash_intensity", flashViews.intensity)
                    .apply();
        }
    }

    public void setIconMuted(boolean muted, boolean animated) {
        if (muteButtonDrawable == null) {
            muteButtonDrawable = new RLottieDrawable(R.raw.media_mute_unmute, "media_mute_unmute", AndroidUtilities.dp(28), AndroidUtilities.dp(28), true, null);
            muteButtonDrawable.multiplySpeed(1.5f);
        }
        muteButton.setAnimation(muteButtonDrawable);
        if (!animated) {
            muteButtonDrawable.setCurrentFrame(muted ? 20 : 0, false);
            return;
        }
        if (muted) {
            if (muteButtonDrawable.getCurrentFrame() > 20) {
                muteButtonDrawable.setCurrentFrame(0, false);
            }
            muteButtonDrawable.setCustomEndFrame(20);
            muteButtonDrawable.start();
        } else {
            if (muteButtonDrawable.getCurrentFrame() == 0 || muteButtonDrawable.getCurrentFrame() >= 43) {
                return;
            }
            muteButtonDrawable.setCustomEndFrame(43);
            muteButtonDrawable.start();
        }
    }

    private void showPremiumPeriodBulletin(int period) {
        final int hours = period / 3600;
        Activity activity = parentAlert.baseFragment.getParentActivity();
        Bulletin.BulletinWindow.BulletinWindowLayout window = Bulletin.BulletinWindow.make(activity, new Bulletin.Delegate() {
            @Override
            public int getTopOffset(int tag) {
                return 0;
            }

            @Override
            public boolean clipWithGradient(int tag) {
                return true;
            }
        });
        WindowManager.LayoutParams params = window.getLayout();
        if (params != null) {
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = containerView.getWidth();
            params.y = (int) (containerView.getY() + AndroidUtilities.dp(56));
            window.updateLayout();
        }
        window.setTouchable(true);
        BulletinFactory.of(window, resourcesProvider)
                .createSimpleBulletin(R.raw.fire_on, premiumText(LocaleController.formatPluralString("StoryPeriodPremium", hours)), 3)
                .show(true);
    }

    private CharSequence premiumText(String text) {
        return AndroidUtilities.replaceSingleTag(text, Theme.key_chat_messageLinkIn, 0, this::openPremium, resourcesProvider);
    }

    private void updateActionBarButtons(boolean animated) {
        showVideoTimer(currentPage == PAGE_CAMERA && isVideo && !collageListView.isVisible() && !inCheck(), animated);
        collageButton.setSelected(collageLayoutView.hasLayout());
        setActionBarButtonVisible(backButton, collageListView == null || !collageListView.isVisible(), animated);
        setActionBarButtonVisible(flashButton, !animatedRecording && currentPage == PAGE_CAMERA && flashButtonMode != null && !collageListView.isVisible() && !inCheck(), animated);
        setActionBarButtonVisible(dualButton, !animatedRecording && currentPage == PAGE_CAMERA && cameraView != null && cameraView.dualAvailable() && !collageListView.isVisible() && !collageLayoutView.hasLayout(), animated);
        setActionBarButtonVisible(collageButton, currentPage == PAGE_CAMERA && !collageListView.isVisible(), animated);
        setActionBarButtonVisible(collageRemoveButton, collageListView.isVisible(), animated);
        final float collageProgress = collageLayoutView.hasLayout() ? collageLayoutView.getFilledProgress() : 0.0f;
        recordControl.setCollageProgress(collageProgress, animated);
        removeCollageHint.show(collageListView.isVisible());
        animateRecording(animatedRecording, animated);
    }

    private void openPremium() {
        if (previewView != null) {
            previewView.updatePauseReason(4, true);
        }
        if (captionEdit != null) {
            captionEdit.hidePeriodPopup();
        }
        PremiumFeatureBottomSheet sheet = new PremiumFeatureBottomSheet(new BaseFragment() {
            @Override
            public Dialog showDialog(Dialog dialog) {
                dialog.show();
                return dialog;
            }

            @Override
            public Theme.ResourcesProvider getResourceProvider() {
                return new WrappedResourceProvider(resourcesProvider) {
                    @Override
                    public void appendColors() {
                        sparseIntArray.append(Theme.key_dialogBackground, 0xFF1E1E1E);
                        sparseIntArray.append(Theme.key_windowBackgroundGray, 0xFF000000);
                    }
                };
            }

            @Override
            public boolean isLightStatusBar() {
                return false;
            }
        }, PremiumPreviewFragment.PREMIUM_FEATURE_STORIES, false);
        sheet.setOnDismissListener(d -> {
            if (previewView != null) {
                previewView.updatePauseReason(4, false);
            }
        });
        sheet.show();
    }

    public void invalidateBlur() {
        if (captionEdit != null) {
            captionEdit.invalidateBlur();
        }
    }

    private boolean requestAudioPermission() {
        Activity activity = parentAlert.baseFragment.getParentActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null) {
            boolean granted = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 112);
                return false;
            }
        }
        return true;
    }

    private boolean requestedCameraPermission;
    private void requestCameraPermission(boolean force) {
        if (requestedCameraPermission && !force) {
            return;
        }
        noCameraPermission = false;

        Activity activity = parentAlert.baseFragment.getParentActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null) {
            noCameraPermission = activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
            if (noCameraPermission) {
                Drawable iconDrawable = getContext().getResources().getDrawable(R.drawable.story_camera).mutate();
                iconDrawable.setColorFilter(new PorterDuffColorFilter(0x3dffffff, PorterDuff.Mode.MULTIPLY));
                CombinedDrawable drawable = new CombinedDrawable(new ColorDrawable(0xff222222), iconDrawable);
                drawable.setIconSize(dp(64), dp(64));
                cameraViewThumb.setImageDrawable(drawable);
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    new AlertDialog.Builder(getContext(), resourcesProvider)
                            .setTopAnimation(R.raw.permission_request_camera, AlertsCreator.PERMISSIONS_REQUEST_TOP_ICON_SIZE, false, Theme.getColor(Theme.key_dialogTopBackground))
                            .setMessage(AndroidUtilities.replaceTags(getString(R.string.PermissionNoCameraWithHint)))
                            .setPositiveButton(getString(R.string.PermissionOpenSettings), (dialogInterface, i) -> {
                                try {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                                    activity.startActivity(intent);
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                            })
                            .setNegativeButton(getString(R.string.ContactsPermissionAlertNotNow), null)
                            .create()
                            .show();
                    return;
                }
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 111);
                requestedCameraPermission = true;
            }
        }

        if (!noCameraPermission) {
            if (CameraController.getInstance().isCameraInitied()) {
                showCamera();
            } else {
                CameraController.getInstance().initCamera(this::showCamera);
            }
        }
    }

    private void orderPreviewViews() {
        if (paintViewRenderView != null) {
            paintViewRenderView.bringToFront();
        }
        if (paintViewRenderInputView != null) {
            paintViewRenderInputView.bringToFront();
        }
        if (paintViewTextDim != null) {
            paintViewTextDim.bringToFront();
        }
        if (paintViewEntitiesView != null) {
            paintViewEntitiesView.bringToFront();
        }
        if (paintViewSelectionContainerView != null) {
            paintViewSelectionContainerView.bringToFront();
        }
        if (trash != null) {
            trash.bringToFront();
        }
        if (photoFilterEnhanceView != null) {
            photoFilterEnhanceView.bringToFront();
        }
        if (photoFilterViewBlurControl != null) {
            photoFilterViewBlurControl.bringToFront();
        }
        if (photoFilterViewCurvesControl != null) {
            photoFilterViewCurvesControl.bringToFront();
        }
        if (previewHighlight != null) {
            previewHighlight.bringToFront();
        }
        if (currentRoundRecorder != null) {
            currentRoundRecorder.bringToFront();
        }
    }

    private AnimatorSet editModeAnimator;

    public void switchToEditMode(int editMode, boolean animated) {
        switchToEditMode(editMode,
                false,
                animated);
    }

    public void switchToEditMode(int editMode, boolean force, boolean animated) {
        if (currentEditMode == editMode && !force) {
            return;
        }
        if (editMode != EDIT_MODE_NONE && (captionEdit != null && captionEdit.isRecording())) {
            return;
        }

        final int oldEditMode = currentEditMode;
        currentEditMode = editMode;

        if (editModeAnimator != null) {
            editModeAnimator.cancel();
            editModeAnimator = null;
        }

        previewButtons.appear((editMode == EDIT_MODE_NONE || editMode == EDIT_MODE_TIMELINE) && openProgress > 0, animated);

        ArrayList<Animator> animators = new ArrayList<>();

        boolean delay = photoFilterView == null && editMode == EDIT_MODE_FILTER;
        if (editMode == EDIT_MODE_FILTER) {
            createFilterPhotoView();
//            animatePhotoFilterTexture(true, animated);
            previewTouchable = photoFilterView;
            View toolsView = photoFilterView != null ? photoFilterView.getToolsView() : null;
            if (toolsView != null) {
                toolsView.setAlpha(0f);
                toolsView.setVisibility(View.VISIBLE);
                animators.add(ObjectAnimator.ofFloat(toolsView, View.TRANSLATION_Y, 0));
                animators.add(ObjectAnimator.ofFloat(toolsView, View.ALPHA, 1));
            }
        } else if (oldEditMode == EDIT_MODE_FILTER && photoFilterView != null) {
            previewTouchable = null;
//            animatePhotoFilterTexture(false, animated);
            animators.add(ObjectAnimator.ofFloat(photoFilterView.getToolsView(), View.TRANSLATION_Y, dp(186 + 40)));
            animators.add(ObjectAnimator.ofFloat(photoFilterView.getToolsView(), View.ALPHA, 0));
        }

        if (editMode == EDIT_MODE_PAINT) {
            createPhotoPaintView();
            previewTouchable = paintView;
            animators.add(ObjectAnimator.ofFloat(backButton, View.ALPHA, 0));
            animators.add(ObjectAnimator.ofFloat(paintView.getTopLayout(), View.ALPHA, 0, 1));
            animators.add(ObjectAnimator.ofFloat(paintView.getTopLayout(), View.TRANSLATION_Y, -AndroidUtilities.dp(16), 0));
            animators.add(ObjectAnimator.ofFloat(paintView.getBottomLayout(), View.ALPHA, 0, 1));
            animators.add(ObjectAnimator.ofFloat(paintView.getBottomLayout(), View.TRANSLATION_Y, AndroidUtilities.dp(48), 0));
            animators.add(ObjectAnimator.ofFloat(paintView.getWeightChooserView(), View.TRANSLATION_X, -AndroidUtilities.dp(32), 0));
        } else if (oldEditMode == EDIT_MODE_PAINT && paintView != null) {
            previewTouchable = null;
            animators.add(ObjectAnimator.ofFloat(backButton, View.ALPHA, 1));
            animators.add(ObjectAnimator.ofFloat(paintView.getTopLayout(), View.ALPHA, 0));
            animators.add(ObjectAnimator.ofFloat(paintView.getTopLayout(), View.TRANSLATION_Y, -AndroidUtilities.dp(16)));
            animators.add(ObjectAnimator.ofFloat(paintView.getBottomLayout(), View.ALPHA, 0));
            animators.add(ObjectAnimator.ofFloat(paintView.getBottomLayout(), View.TRANSLATION_Y, AndroidUtilities.dp(48)));
            animators.add(ObjectAnimator.ofFloat(paintView.getWeightChooserView(), View.TRANSLATION_X, -AndroidUtilities.dp(32)));
        }

        animators.add(ObjectAnimator.ofFloat(muteButton, View.ALPHA, (editMode == EDIT_MODE_NONE || editMode == EDIT_MODE_TIMELINE) && isVideo ? 1 : 0));
        animators.add(ObjectAnimator.ofFloat(playButton, View.ALPHA, (editMode == EDIT_MODE_NONE || editMode == EDIT_MODE_TIMELINE) && (isVideo || outputEntry != null && !TextUtils.isEmpty(outputEntry.audioPath)) ? 1 : 0));
        animators.add(ObjectAnimator.ofFloat(downloadButton, View.ALPHA, (editMode == EDIT_MODE_NONE || editMode == EDIT_MODE_TIMELINE) ? 1 : 0));
        if (themeButton != null) {
            animators.add(ObjectAnimator.ofFloat(themeButton, View.ALPHA, (editMode == EDIT_MODE_NONE || editMode == EDIT_MODE_TIMELINE) && (outputEntry != null && outputEntry.isRepostMessage) ? 1f : 0));
        }
        animators.add(ObjectAnimator.ofFloat(titleTextView, View.ALPHA, (currentPage == PAGE_PREVIEW || currentPage == PAGE_COVER) && editMode == EDIT_MODE_NONE ? 1f : 0f));

        int rightMargin = 0;
        int bottomMargin = 0;
        if (editMode == EDIT_MODE_FILTER) {
            previewContainer.setPivotY(previewContainer.getMeasuredHeight() * .2f);
            bottomMargin = dp(164);
        } else if (editMode == EDIT_MODE_PAINT) {
            previewContainer.setPivotY(previewContainer.getMeasuredHeight() * .6f);
            bottomMargin = dp(40);
        } else if (editMode == EDIT_MODE_TIMELINE) {
            previewContainer.setPivotY(0);
            bottomMargin = timelineView.getContentHeight() + dp(8);
//            rightMargin = dp(46);
        }

        float scale = 1f;
        if (bottomMargin > 0) {
            final int bottomPivot = previewContainer.getHeight() - (int) previewContainer.getPivotY();
            scale = (float) (bottomPivot - bottomMargin) / bottomPivot;
        }
        if (rightMargin > 0) {
            final int rightPivot = previewContainer.getWidth() - (int) previewContainer.getPivotX();
            scale = Math.min(scale, (float) (rightPivot - rightMargin) / rightPivot);
        }

        animators.add(ObjectAnimator.ofFloat(previewContainer, View.SCALE_X, scale));
        animators.add(ObjectAnimator.ofFloat(previewContainer, View.SCALE_Y, scale));
        if (editMode == EDIT_MODE_NONE) {
            animators.add(ObjectAnimator.ofFloat(previewContainer, View.TRANSLATION_Y, 0));
        }

        if (photoFilterViewCurvesControl != null) {
            animators.add(ObjectAnimator.ofFloat(photoFilterViewCurvesControl, View.ALPHA, editMode == EDIT_MODE_FILTER ? 1f : 0));
        }
        if (photoFilterViewBlurControl != null) {
            animators.add(ObjectAnimator.ofFloat(photoFilterViewBlurControl, View.ALPHA, editMode == EDIT_MODE_FILTER ? 1f : 0));
        }

        animators.add(ObjectAnimator.ofFloat(captionEdit, View.ALPHA, editMode == EDIT_MODE_NONE ? 1f : 0));
        animators.add(ObjectAnimator.ofFloat(videoTimelineContainerView, View.ALPHA, editMode == EDIT_MODE_NONE || editMode == EDIT_MODE_TIMELINE ? 1f : 0));
        animators.add(ObjectAnimator.ofFloat(videoTimelineContainerView, View.TRANSLATION_Y, editMode == EDIT_MODE_TIMELINE ? dp(68) : -(captionEdit.getEditTextHeight() + AndroidUtilities.dp(12)) + AndroidUtilities.dp(64)));
        actionBarButtons.setPivotX(actionBarButtons.getMeasuredWidth() - dp(46 / 2.0f));
        animators.add(ObjectAnimator.ofFloat(actionBarButtons, View.ROTATION, editMode == EDIT_MODE_TIMELINE ? -90 : 0));
        animators.add(ObjectAnimator.ofFloat(playButton, View.ROTATION, editMode == EDIT_MODE_TIMELINE ? 90 : 0));
        animators.add(ObjectAnimator.ofFloat(muteButton, View.ROTATION, editMode == EDIT_MODE_TIMELINE ? 90 : 0));
        animators.add(ObjectAnimator.ofFloat(downloadButton, View.ROTATION, editMode == EDIT_MODE_TIMELINE ? 90 : 0));
        if (themeButton != null) {
            animators.add(ObjectAnimator.ofFloat(themeButton, View.ROTATION, editMode == EDIT_MODE_TIMELINE ? 90 : 0));
        }
        if (blurManager.hasRenderNode()) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                    captionEdit.invalidateBlur();
                }
            });
            animators.add(valueAnimator);
        }

        if (oldEditMode != editMode) {
            onSwitchEditModeStart(oldEditMode, editMode);
        }
        if (timelineView != null) {
            timelineView.setOpen(outputEntry == null || !outputEntry.isCollage() || !outputEntry.hasVideo() || editMode == EDIT_MODE_TIMELINE, animated);
        }
        if (animated) {
            editModeAnimator = new AnimatorSet();
            editModeAnimator.playTogether(animators);
            editModeAnimator.setDuration(320);
            editModeAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            editModeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (oldEditMode != editMode) {
                        onSwitchEditModeEnd(oldEditMode, editMode);
                    }
                }
            });
            if (delay) {
                editModeAnimator.setStartDelay(120L);
            }
            editModeAnimator.start();
        } else {
            for (int i = 0; i < animators.size(); ++i) {
                Animator a = animators.get(i);
                a.setDuration(1);
                a.start();
            }
            if (oldEditMode != editMode) {
                onSwitchEditModeEnd(oldEditMode, editMode);
            }
        }
    }

    private void applyFilterMatrix() {
        if (outputEntry != null && photoFilterViewTextureView != null && previewContainer.getMeasuredWidth() > 0 && previewContainer.getMeasuredHeight() > 0) {
            Matrix photoFilterStartMatrix = new Matrix();
            photoFilterStartMatrix.reset();
            if (outputEntry.orientation != 0) {
                photoFilterStartMatrix.postRotate(-outputEntry.orientation, previewContainer.getMeasuredWidth() / 2f, previewContainer.getMeasuredHeight() / 2f);
                if (outputEntry.orientation / 90 % 2 == 1) {
                    photoFilterStartMatrix.postScale(
                            (float) previewContainer.getMeasuredWidth() / previewContainer.getMeasuredHeight(),
                            (float) previewContainer.getMeasuredHeight() / previewContainer.getMeasuredWidth(),
                            previewContainer.getMeasuredWidth() / 2f,
                            previewContainer.getMeasuredHeight() / 2f
                    );
                }
            }
            photoFilterStartMatrix.postScale(
                    1f / previewContainer.getMeasuredWidth() * outputEntry.width,
                    1f / previewContainer.getMeasuredHeight() * outputEntry.height
            );
            photoFilterStartMatrix.postConcat(outputEntry.matrix);
            photoFilterStartMatrix.postScale(
                    (float) previewContainer.getMeasuredWidth() / outputEntry.resultWidth,
                    (float) previewContainer.getMeasuredHeight() / outputEntry.resultHeight
            );
            photoFilterViewTextureView.setTransform(photoFilterStartMatrix);
            photoFilterViewTextureView.invalidate();
        }
    }

    private void hidePhotoPaintView() {
        if (paintView == null) {
            return;
        }
        previewTouchable = null;
        paintView.getTopLayout().setAlpha(0f);
        paintView.getTopLayout().setTranslationY(-AndroidUtilities.dp(16));
        paintView.getBottomLayout().setAlpha(0f);
        paintView.getBottomLayout().setTranslationY(AndroidUtilities.dp(48));
        paintView.getWeightChooserView().setTranslationX(-AndroidUtilities.dp(32));
        paintView.setVisibility(View.GONE);
//        paintView.keyboardNotifier.ignore(true);
    }

    private Runnable audioGrantedCallback;
    private void createPhotoPaintView() {
        if (paintView != null) {
            return;
        }
        Pair<Integer, Integer> size = previewView.getPaintSize();

        Bitmap paintViewBitmap = null;
        if (outputEntry != null && (outputEntry.isDraft || outputEntry.isEdit) && outputEntry.paintFile != null) {
            paintViewBitmap = BitmapFactory.decodeFile(outputEntry.paintFile.getPath());
        }
        if (paintViewBitmap == null) {
            paintViewBitmap = Bitmap.createBitmap(size.first, size.second, Bitmap.Config.ARGB_8888);
        }

        boolean hasBlur = false;
        Bitmap paintViewBlurBitmap = null;
        if (outputEntry != null && (outputEntry.isDraft || outputEntry.isEdit) && outputEntry.paintBlurFile != null) {
            paintViewBlurBitmap = BitmapFactory.decodeFile(outputEntry.paintBlurFile.getPath());
            if (paintViewBlurBitmap != null) {
                hasBlur = true;
            }
        }
        if (paintViewBlurBitmap == null) {
            paintViewBlurBitmap = Bitmap.createBitmap(size.first, size.second, Bitmap.Config.ARGB_8888);
        }

        int w = previewContainer.getMeasuredWidth(), h = previewContainer.getMeasuredHeight();
        Activity activity = parentAlert.baseFragment.getParentActivity();
        paintView = new PaintView(
                activity,
                outputEntry != null && !outputEntry.fileDeletable,
                outputEntry == null ? null : outputEntry.file,
                outputEntry != null && outputEntry.isVideo,
                outputEntry != null && outputEntry.botId != 0,
                windowView,
                activity,
                currentAccount,
                paintViewBitmap,
                paintViewBlurBitmap,
                null,
                previewView.getOrientation(),
                outputEntry == null ? null : outputEntry.mediaEntities,
                outputEntry,
                w, h,
                new MediaController.CropState(),
                null,
                blurManager,
                resourcesProvider,
                videoTextureHolder,
                previewView
        ) {
            @Override
            public void onEntityDraggedTop(boolean value) {
                previewHighlight.show(true, value, actionBarContainer);
            }

            @Override
            protected void onGalleryClick() {
                captionEdit.keyboardNotifier.ignore(true);
                destroyGalleryListView();
                createGalleryListView(true);
                animateGalleryListView(true);
            }

            @Override
            public void onEntityDraggedBottom(boolean value) {
                previewHighlight.updateCaption(captionEdit.getText());
                previewHighlight.show(false, value && multitouch, null);
            }

            @Override
            public void onEntityDragEnd(boolean delete) {
                if (!isEntityDeletable()) {
                    delete = false;
                }
                captionEdit.clearAnimation();
                captionEdit.animate().alpha(currentEditMode == EDIT_MODE_NONE ? 1f : 0).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
                videoTimelineContainerView.clearAnimation();
                videoTimelineContainerView.animate().alpha(currentEditMode == EDIT_MODE_NONE || currentEditMode == EDIT_MODE_TIMELINE ? 1f : 0).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
                showTrash(false, delete);
                if (delete) {
                    removeCurrentEntity();
                }
                super.onEntityDragEnd(delete);
                multitouch = false;
            }

            @Override
            public void onEntityDragStart() {
                paintView.showReactionsLayout(false);
                captionEdit.clearAnimation();
                captionEdit.animate().alpha(0f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
                if (currentEditMode != EDIT_MODE_TIMELINE) {
                    videoTimelineContainerView.clearAnimation();
                    videoTimelineContainerView.animate().alpha(0f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
                }
                showTrash(isEntityDeletable(), false);
            }

            public void showTrash(boolean show, boolean delete) {
                if (show) {
                    trash.setVisibility(View.VISIBLE);
                    trash.setAlpha(0f);
                    trash.clearAnimation();
                    trash.animate().alpha(1f).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
                } else {
                    trash.onDragInfo(false, delete);
                    trash.clearAnimation();
                    trash.animate().alpha(0f).withEndAction(() -> {
                        trash.setVisibility(View.GONE);
                    }).setDuration(180).setInterpolator(CubicBezierInterpolator.EASE_OUT).setStartDelay(delete ? 500 : 0).start();
                }
            }

            private boolean multitouch;

            @Override
            public void onEntityDragMultitouchStart() {
                multitouch = true;
                paintView.showReactionsLayout(false);
                showTrash(false, false);
            }

            @Override
            public void onEntityDragMultitouchEnd() {
                multitouch = false;
                showTrash(isEntityDeletable(), false);
                previewHighlight.show(false, false, null);
            }

            @Override
            public void onEntityDragTrash(boolean enter) {
                trash.onDragInfo(enter, false);
            }

            @Override
            protected void editSelectedTextEntity() {
                captionEdit.editText.closeKeyboard();
                switchToEditMode(EDIT_MODE_PAINT, true);
                super.editSelectedTextEntity();
            }

            @Override
            public void dismiss() {
                captionEdit.editText.closeKeyboard();
                switchToEditMode(EDIT_MODE_NONE, true);
            }

            @Override
            protected void onOpenCloseStickersAlert(boolean open) {
                if (previewView != null) {
                    previewView.updatePauseReason(6, open);
                    if (playButton != null) {
                        playButton.drawable.setPause(previewView.isPlaying(), true);
                    }
                }
                if (captionEdit != null) {
                    captionEdit.ignoreTouches = open;
                    captionEdit.keyboardNotifier.ignore(open);
                }
            }

            @Override
            protected void onAudioSelect(MessageObject messageObject) {
                previewView.setupAudio(messageObject, true);
                if (outputEntry != null && !isVideo) {
                    boolean appear = !TextUtils.isEmpty(outputEntry.audioPath);
                    playButton.drawable.setPause(!previewView.isPlaying(), false);
                    playButton.setVisibility(View.VISIBLE);
                    playButton.animate().alpha(appear ? 1 : 0).withEndAction(() -> {
                        if (!appear) {
                            playButton.setVisibility(View.GONE);
                        }
                    }).start();
                }
                switchToEditMode(collageLayoutView.hasLayout() && collageLayoutView.hasVideo() && !TextUtils.isEmpty(outputEntry.audioPath) ? EDIT_MODE_TIMELINE : EDIT_MODE_NONE, true, true);
            }

            @Override
            public void onEntityHandleTouched() {
                paintView.showReactionsLayout(false);
            }

            @Override
            protected boolean checkAudioPermission(Runnable granted) {
                if (activity == null) {
                    return true;
                }
                if (Build.VERSION.SDK_INT >= 33) {
                    if (activity.checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        activity.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 115);
                        audioGrantedCallback = granted;
                        return false;
                    }
                } else if (Build.VERSION.SDK_INT >= 23 && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 115);
                    audioGrantedCallback = granted;
                    return false;
                }
                return true;
            }

            @Override
            public void onCreateRound(RoundView roundView) {
                if (previewView != null) {
                    previewView.attachRoundView(roundView);
                }
                if (captionEdit != null) {
                    captionEdit.setHasRoundVideo(true);
                }
            }

            @Override
            public void onTryDeleteRound() {
                if (captionEdit != null) {
                    captionEdit.showRemoveRoundAlert();
                }
            }

            @Override
            public void onDeleteRound() {
                if (previewView != null) {
                    previewView.setupRound(null, null, true);
                }
                if (paintView != null) {
                    paintView.deleteRound();
                }
                if (captionEdit != null) {
                    captionEdit.setHasRoundVideo(false);
                }
                if (outputEntry != null) {
                    if (outputEntry.round != null) {
                        try {
                            outputEntry.round.delete();
                        } catch (Exception ignore) {}
                        outputEntry.round = null;
                    }
                    if (outputEntry.roundThumb != null) {
                        try {
                            new File(outputEntry.roundThumb).delete();
                        } catch (Exception ignore) {}
                        outputEntry.roundThumb = null;
                    }
                }
            }

            @Override
            public void onSwitchSegmentedAnimation(PhotoView photoView) {
                if (photoView == null) {
                    return;
                }
                ThanosEffect thanosEffect = getThanosEffect();
                if (thanosEffect == null) {
                    photoView.onSwitchSegmentedAnimationStarted(false);
                    return;
                }
                Bitmap bitmap = photoView.getSegmentedOutBitmap();
                if (bitmap == null) {
                    photoView.onSwitchSegmentedAnimationStarted(false);
                    return;
                }
                Matrix matrix = new Matrix();
                float w = photoView.getWidth(), h = photoView.getHeight();
                float tx = 0, ty = 0;
                if (photoView.getRotation() != 0) {
                    final float bw = bitmap.getWidth();
                    final float bh = bitmap.getHeight();
                    final float r = (float) Math.sqrt((bw / 2f) * (bw / 2f) + (bh / 2f) * (bh / 2f));
                    final float d = 2 * r;
                    Bitmap newBitmap = Bitmap.createBitmap((int) d, (int) d, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(newBitmap);
                    canvas.save();
                    canvas.rotate(photoView.getRotation(), r, r);
                    canvas.drawBitmap(bitmap, (d - bw) / 2, (d - bh) / 2, null);
                    bitmap.recycle();
                    bitmap = newBitmap;

                    final float pd = 2 * (float) Math.sqrt((w / 2f) * (w / 2f) + (h / 2f) * (h / 2f));
                    tx = -(pd - w) / 2;
                    ty = -(pd - h) / 2;
                    w = pd;
                    h = pd;
                }
                matrix.postScale(w, h);
                matrix.postScale(photoView.getScaleX(), photoView.getScaleY(), w / 2f, h / 2f);
                matrix.postTranslate(containerView.getX() + previewContainer.getX() + photoView.getX() + tx, containerView.getY() + previewContainer.getY() + photoView.getY() + ty);
                thanosEffect.animate(matrix, bitmap, () -> {
                    photoView.onSwitchSegmentedAnimationStarted(true);
                }, () -> {});
            }

            @Override
            public void onSelectRound(RoundView roundView) {
                if (timelineView != null) {
                    timelineView.selectRound(true);
                }
            }

            @Override
            public void onDeselectRound(RoundView roundView) {
                if (timelineView != null) {
                    timelineView.selectRound(false);
                }
            }
        };
        paintView.setHasAudio(outputEntry != null && outputEntry.audioPath != null);
        paintView.setBlurManager(blurManager);
        containerView.addView(paintView);
        paintViewRenderView = paintView.getRenderView();
        if (paintViewRenderView != null) {
            paintViewRenderView.getPainting().hasBlur = hasBlur;
            previewContainer.addView(paintViewRenderView);
        }
        paintViewRenderInputView = paintView.getRenderInputView();
        if (paintViewRenderInputView != null) {
            previewContainer.addView(paintViewRenderInputView);
        }
        paintViewTextDim = paintView.getTextDimView();
        if (paintViewTextDim != null) {
            previewContainer.addView(paintViewTextDim);
        }
        paintViewEntitiesView = paintView.getEntitiesView();
        if (paintViewEntitiesView != null) {
            previewContainer.addView(paintViewEntitiesView);
        }
        paintViewSelectionContainerView = paintView.getSelectionEntitiesView();
        if (paintViewSelectionContainerView != null) {
            previewContainer.addView(paintViewSelectionContainerView);
        }
        orderPreviewViews();
        paintView.setOnDoneButtonClickedListener(() -> {
            switchToEditMode(EDIT_MODE_NONE, true);
        });
        paintView.setOnCancelButtonClickedListener(() -> {
            switchToEditMode(EDIT_MODE_NONE, true);
        });
        paintView.init();
    }

    public ThanosEffect getThanosEffect() {
        if (!ThanosEffect.supports()) {
            return null;
        }
        if (thanosEffect == null) {
            windowView.addView(thanosEffect = new ThanosEffect(getContext(), () -> {
                ThanosEffect thisThanosEffect = thanosEffect;
                if (thisThanosEffect != null) {
                    thanosEffect = null;
                    windowView.removeView(thisThanosEffect);
                }
            }));
        }
        return thanosEffect;
    }

    private boolean forceBackgroundVisible;
    private boolean isBackgroundVisible;
    private void checkBackgroundVisibility() {
        boolean shouldBeVisible = dismissProgress != 0 || openProgress < 1 || forceBackgroundVisible;
        if (shouldBeVisible == isBackgroundVisible) {
            return;
        }
        isBackgroundVisible = shouldBeVisible;
    }

    private String getCurrentFlashMode() {
        if (cameraView == null || cameraView.getCameraSession() == null) {
            return null;
        }
        if (cameraView.isFrontface() && !cameraView.getCameraSession().hasFlashModes()) {
            checkFrontfaceFlashModes();
            return frontfaceFlashModes.get(frontfaceFlashMode);
        }
        return cameraView.getCameraSession().getCurrentFlashMode();
    }

    private int frontfaceFlashMode = -1;
    private ArrayList<String> frontfaceFlashModes;
    private void checkFrontfaceFlashModes() {
        if (frontfaceFlashMode < 0) {
            frontfaceFlashMode = MessagesController.getGlobalMainSettings().getInt("frontflash", 1);
            frontfaceFlashModes = new ArrayList<>();
            frontfaceFlashModes.add(Camera.Parameters.FLASH_MODE_OFF);
            frontfaceFlashModes.add(Camera.Parameters.FLASH_MODE_AUTO);
            frontfaceFlashModes.add(Camera.Parameters.FLASH_MODE_ON);

            flashViews.setWarmth(MessagesController.getGlobalMainSettings().getFloat("frontflash_warmth", .9f));
            flashViews.setIntensity(MessagesController.getGlobalMainSettings().getFloat("frontflash_intensity", 1));
        }
    }

    private String getNextFlashMode() {
        if (cameraView == null || cameraView.getCameraSession() == null) {
            return null;
        }
        if (cameraView.isFrontface() && !cameraView.getCameraSession().hasFlashModes()) {
            checkFrontfaceFlashModes();
            return frontfaceFlashModes.get(frontfaceFlashMode + 1 >= frontfaceFlashModes.size() ? 0 : frontfaceFlashMode + 1);
        }
        return cameraView.getCameraSession().getNextFlashMode();
    }

    private void setCurrentFlashMode(String mode) {
        if (cameraView == null || cameraView.getCameraSession() == null) {
            return;
        }
        if (cameraView.isFrontface() && !cameraView.getCameraSession().hasFlashModes()) {
            int index = frontfaceFlashModes.indexOf(mode);
            if (index >= 0) {
                frontfaceFlashMode = index;
                MessagesController.getGlobalMainSettings().edit().putInt("frontflash", frontfaceFlashMode).apply();
            }
            return;
        }
        cameraView.getCameraSession().setCurrentFlashMode(mode);
    }

    public void close(boolean animated) {
        if (previewView != null && !animated) {
            previewView.set(null);
        }
    }

    public class WindowView extends WindowViewAbstract {

        private GestureDetectorFixDoubleTap gestureDetector;
        private ScaleGestureDetector scaleGestureDetector;

        public WindowView(Context context) {
            super(context);
            gestureDetector = new GestureDetectorFixDoubleTap(context, new WindowView.GestureListener());
            scaleGestureDetector = new ScaleGestureDetector(context, new WindowView.ScaleListener());
        }

        private int lastKeyboardHeight;

        @Override
        public int getBottomPadding() {
            return getHeight() - containerView.getBottom() + underControls;
        }

        public int getBottomPadding2() {
            return getHeight() - containerView.getBottom();
        }

        public int getPaddingUnderContainer() {
            return getHeight() - insetBottom - containerView.getBottom();
        }


        private boolean flingDetected;
        private boolean touchInCollageList;

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            flingDetected = false;
            if (collageListView != null && collageListView.isVisible()) {
                final float y = containerView.getY() + actionBarContainer.getY() + collageListView.getY();
                if (ev.getY() >= y && ev.getY() <= y + collageListView.getHeight() || touchInCollageList) {
                    touchInCollageList = ev.getAction() != MotionEvent.ACTION_UP && ev.getAction() != MotionEvent.ACTION_CANCEL;
                    return super.dispatchTouchEvent(ev);
                } else {
                    collageListView.setVisible(false, true);
                    updateActionBarButtons(true);
                }
            }
            if (touchInCollageList && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL)) {
                touchInCollageList = false;
            }
            scaleGestureDetector.onTouchEvent(ev);
            gestureDetector.onTouchEvent(ev);
            if (ev.getAction() == MotionEvent.ACTION_UP && !flingDetected) {
                allowModeScroll = true;
                if (containerView.getTranslationY() > 0) {
                    if (dismissProgress > .4f) {
                        close(true);
                    } else {
                        animateContainerBack();
                    }
                } else if (galleryListView != null && galleryListView.getTranslationY() > 0 && !galleryClosing) {
                    animateGalleryListView(!takingVideo && galleryListView.getTranslationY() < galleryListView.getPadding());
                }
                galleryClosing = false;
                modeSwitcherView.stopScroll(0);
                scrollingY = false;
                scrollingX = false;
            }
            return super.dispatchTouchEvent(ev);
        }

        public void cancelGestures() {
            scaleGestureDetector.onTouchEvent(AndroidUtilities.emptyMotionEvent());
            gestureDetector.onTouchEvent(AndroidUtilities.emptyMotionEvent());
        }

        @Override
        public boolean dispatchKeyEventPreIme(KeyEvent event) {
            if (event != null && event.getKeyCode()
                    == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                onBackPressedChecked();
                return true;
            }
            return super.dispatchKeyEventPreIme(event);
        }

        private boolean scaling = false;
        private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (!scaling || cameraView == null || currentPage != PAGE_CAMERA || cameraView.isDualTouch() || collageLayoutView.getFilledProgress() >= 1) {
                    return false;
                }
                final float deltaScaleFactor = (detector.getScaleFactor() - 1.0f) * .75f;
                cameraZoom += deltaScaleFactor;
                cameraZoom = Utilities.clamp(cameraZoom, 1, 0);
                cameraView.setZoom(cameraZoom);
                if (zoomControlView != null) {
                    zoomControlView.setZoom(cameraZoom, false);
                }
                showZoomControls(true, true);
                return true;
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                if (cameraView == null || currentPage != PAGE_CAMERA || wasGalleryOpen) {
                    return false;
                }
                scaling = true;
                return super.onScaleBegin(detector);
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                scaling = false;
                animateGalleryListView(false);
                animateContainerBack();
                super.onScaleEnd(detector);
            }
        }

        private float ty, sty, stx;
        private boolean allowModeScroll = true;

        private final class GestureListener extends GestureDetectorFixDoubleTap.OnGestureListener {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                sty = 0;
                stx = 0;
                return false;
            }

            @Override
            public void onShowPress(@NonNull MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                scrollingY = false;
                scrollingX = false;
                if (!hasDoubleTap(e)) {
                    if (onSingleTapConfirmed(e)) {
                        return true;
                    }
                }
                if (isGalleryOpen() && e.getY() < galleryListView.top()) {
                    animateGalleryListView(false);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                if (cameraAnimationInProgress || galleryOpenCloseSpringAnimator != null || galleryOpenCloseAnimator != null || recordControl.isTouch() || cameraView != null && cameraView.isDualTouch() || scaling || zoomControlView != null && zoomControlView.isTouch() || inCheck()) {
                    return false;
                }
                if (takingVideo || takingPhoto || currentPage != PAGE_CAMERA) {
                    return false;
                }
                if (!scrollingX) {
                    sty += distanceY;
                    if (!scrollingY && Math.abs(sty) >= touchSlop) {
                        if (collageLayoutView != null) {
                            collageLayoutView.cancelTouch();
                        }
                        scrollingY = true;
                    }
                }
                if (scrollingY) {
                    int galleryMax = windowView.getMeasuredHeight() - (int) (AndroidUtilities.displaySize.y * 0.35f) - (AndroidUtilities.statusBarHeight + ActionBar.getCurrentActionBarHeight());
                    if (galleryListView == null || galleryListView.getTranslationY() >= galleryMax) {
                        ty = containerView.getTranslationY1();
                    } else {
                        ty = galleryListView.getTranslationY() - galleryMax;
                    }
                    if (galleryListView != null && galleryListView.listView.canScrollVertically(-1)) {
                        distanceY = Math.max(0, distanceY);
                    }
                    ty -= distanceY;
                    ty = Math.max(-galleryMax, ty);
                    if (currentPage == PAGE_PREVIEW) {
                        ty = Math.max(0, ty);
                    }
                    if (ty >= 0) {
                        containerView.setTranslationY(ty);
                        if (galleryListView != null) {
                            galleryListView.setTranslationY(galleryMax);
                        }
                    } else {
                        containerView.setTranslationY(0);
                        if (galleryListView == null) {
                            createGalleryListView();
                        }
                        galleryListView.setTranslationY(galleryMax + ty);
                    }
                }
                if (!scrollingY) {
                    stx += distanceX;
                    if (!scrollingX && Math.abs(stx) >= touchSlop) {
                        if (collageLayoutView != null) {
                            collageLayoutView.cancelTouch();
                        }
                        scrollingX = true;
                    }
                }
                if (scrollingX) {
                    modeSwitcherView.scrollX(distanceX);
                }
                return true;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {

            }

            @Override
            public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (cameraAnimationInProgress || recordControl.isTouch() || cameraView != null && cameraView.isDualTouch() || scaling || zoomControlView != null && zoomControlView.isTouch() || inCheck()) {
                    return false;
                }
                flingDetected = true;
                allowModeScroll = true;
                boolean r = false;
                if (scrollingY) {
                    if (Math.abs(containerView.getTranslationY1()) >= dp(1)) {
                        if (velocityY > 0 && Math.abs(velocityY) > 2000 && Math.abs(velocityY) > Math.abs(velocityX) || dismissProgress > .4f) {
                            close(true);
                        } else {
                            animateContainerBack();
                        }
                        r = true;
                    } else if (galleryListView != null && !galleryClosing) {
                        if (Math.abs(velocityY) > 200 && (!galleryListView.listView.canScrollVertically(-1) || !wasGalleryOpen)) {
                            animateGalleryListView(!takingVideo && velocityY < 0);
                            r = true;
                        } else {
                            animateGalleryListView(!takingVideo && galleryListView.getTranslationY() < galleryListView.getPadding());
                            r = true;
                        }
                    }
                }
                if (scrollingX) {
                    r = modeSwitcherView.stopScroll(velocityX) || r;
                }
                galleryClosing = false;
                scrollingY = false;
                scrollingX = false;
                if (r && collageLayoutView != null) {
                    collageLayoutView.cancelTouch();
                }
                return r;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (cameraView != null) {
                    cameraView.allowToTapFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (cameraView == null || awaitingPlayer || takingPhoto || !cameraView.isInited() || currentPage != PAGE_CAMERA) {
                    return false;
                }
                cameraView.switchCamera();
                recordControl.rotateFlip(180);
                saveCameraFace(cameraView.isFrontface());
                if (useDisplayFlashlight()) {
                    flashViews.flashIn(null);
                } else {
                    flashViews.flashOut();
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                if (cameraView != null) {
                    cameraView.clearTapFocus();
                }
                return false;
            }

            @Override
            public boolean hasDoubleTap(MotionEvent e) {
                return currentPage == PAGE_CAMERA && cameraView != null && !awaitingPlayer && cameraView.isInited() && !takingPhoto && !recordControl.isTouch() && !isGalleryOpen() && galleryListViewOpening == null;
            }
        };

        private boolean ignoreLayout;

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (Build.VERSION.SDK_INT < 21) {
                insetTop = AndroidUtilities.statusBarHeight;
                insetBottom = AndroidUtilities.navigationBarHeight;
            }

            final int W = MeasureSpec.getSize(widthMeasureSpec);
            final int H = MeasureSpec.getSize(heightMeasureSpec);
            final int w = W - insetLeft - insetRight;

            final int statusbar = insetTop;
            final int navbar = insetBottom;

            final int hFromW = (int) Math.ceil(w / 9f * 16f);
            underControls = dp(48);
            if (hFromW + underControls <= H - navbar) {
                previewW = w;
                previewH = H;
                underStatusBar = previewH + underControls > H - navbar - statusbar;
            } else {
                underStatusBar = false;
                previewH = H;
                previewW = w;
            }
            underControls = Utilities.clamp(H - previewH - (underStatusBar ? 0 : statusbar), dp(68), dp(48));

            setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_FULLSCREEN
            );

            containerView.measure(
                    MeasureSpec.makeMeasureSpec(previewW, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(previewH + underControls, MeasureSpec.EXACTLY)
            );
            flashViews.backgroundView.measure(
                    MeasureSpec.makeMeasureSpec(W, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(H, MeasureSpec.EXACTLY)
            );
            if (thanosEffect != null) {
                thanosEffect.measure(
                        MeasureSpec.makeMeasureSpec(W, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(H, MeasureSpec.EXACTLY)
                );
            }
            if (changeDayNightView != null) {
                changeDayNightView.measure(
                        MeasureSpec.makeMeasureSpec(W, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(H, MeasureSpec.EXACTLY)
                );
            }
            if (themeSheet != null) {
                themeSheet.measure(widthMeasureSpec, heightMeasureSpec);
            }

            if (galleryListView != null) {
                galleryListView.measure(MeasureSpec.makeMeasureSpec(previewW, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(H, MeasureSpec.EXACTLY));
            }

            if (captionEdit != null) {
                EmojiView emojiView = captionEdit.editText.getEmojiView();
                if (measureKeyboardHeight() > AndroidUtilities.dp(20)) {
                    ignoreLayout = true;
//                    captionEdit.editText.hideEmojiView();
                    ignoreLayout = false;
                }
                if (emojiView != null) {
                    emojiView.measure(
                            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(emojiView.getLayoutParams().height, MeasureSpec.EXACTLY)
                    );
                }
            }

            if (paintView != null) {
                if (paintView.emojiView != null) {
                    paintView.emojiView.measure(
                            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(paintView.emojiView.getLayoutParams().height, MeasureSpec.EXACTLY)
                    );
                }
                if (paintView.reactionLayout != null) {
                    measureChild(paintView.reactionLayout, widthMeasureSpec, heightMeasureSpec);
                    if (paintView.reactionLayout.getReactionsWindow() != null) {
                        measureChild(paintView.reactionLayout.getReactionsWindow().windowView, widthMeasureSpec, heightMeasureSpec);
                    }
                }
            }

            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child instanceof DownloadButton.PreparingVideoToast) {
                    child.measure(
                            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(H, MeasureSpec.EXACTLY)
                    );
                } else if (child instanceof Bulletin.ParentLayout) {
                    child.measure(
                            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(Math.min(dp(340), H - (underStatusBar ? 0 : statusbar)), MeasureSpec.EXACTLY)
                    );
                }
            }

            setMeasuredDimension(W, H);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (ignoreLayout) {
                return;
            }
            final int W = right - left;
            final int H = bottom - top;

            final int statusbar = insetTop;
            final int underControls = navbarContainer.getMeasuredHeight();

            final int T = underStatusBar ? 0 : statusbar;
            int l = insetLeft + (W - insetRight - previewW) / 2,
                    r = insetLeft + (W - insetRight + previewW) / 2, t, b;
            if (underStatusBar) {
                t = T;
                b = T + previewH + underControls;
            } else {
                t = T + ((H - T - insetBottom) - previewH - underControls) / 2;
                if (openType == 1 && fromRect.top + previewH + underControls < H - insetBottom) {
                    t = (int) fromRect.top;
                } else if (t - T < dp(40)) {
                    t = T;
                }
                b = t + previewH + underControls;
            }

            containerView.layout(0, 0, W, H);
            flashViews.backgroundView.layout(0, 0, W, H);
            if (thanosEffect != null) {
                thanosEffect.layout(0, 0, W, H);
            }
            if (changeDayNightView != null) {
                changeDayNightView.layout(0, 0, W, H);
            }

            if (galleryListView != null) {
                galleryListView.layout((W - galleryListView.getMeasuredWidth()) / 2, 0, (W + galleryListView.getMeasuredWidth()) / 2, H);
            }
            if (themeSheet != null) {
                themeSheet.layout((W - themeSheet.getMeasuredWidth()) / 2, H - themeSheet.getMeasuredHeight(), (W + themeSheet.getMeasuredWidth()) / 2, H);
            }

            if (captionEdit != null) {
                EmojiView emojiView = captionEdit.editText.getEmojiView();
                if (emojiView != null) {
                    emojiView.layout(insetLeft, H - insetBottom - emojiView.getMeasuredHeight(), W - insetRight, H - insetBottom);
                }
            }

            if (paintView != null) {
                if (paintView.emojiView != null) {
                    paintView.emojiView.layout(insetLeft, H - insetBottom - paintView.emojiView.getMeasuredHeight(), W - insetRight, H - insetBottom);
                }
                if (paintView.reactionLayout != null) {
                    paintView.reactionLayout.layout(insetLeft, insetTop, insetLeft + paintView.reactionLayout.getMeasuredWidth(), insetTop + paintView.reactionLayout.getMeasuredHeight());
                    View reactionsWindowView = paintView.reactionLayout.getReactionsWindow() != null ? paintView.reactionLayout.getReactionsWindow().windowView : null;
                    if (reactionsWindowView != null) {
                        reactionsWindowView.layout(insetLeft, insetTop, insetLeft + reactionsWindowView.getMeasuredWidth(), insetTop + reactionsWindowView.getMeasuredHeight());
                    }
                }
            }

            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child instanceof DownloadButton.PreparingVideoToast) {
                    child.layout(0, 0, W, H);
                } else if (child instanceof Bulletin.ParentLayout) {
                    child.layout(0, t, child.getMeasuredWidth(), t + child.getMeasuredHeight());
                }
            }
        }

        @Override
        public void drawBlurBitmap(Bitmap bitmap, float amount) {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(0xff000000);
            final float scale = (float) bitmap.getWidth() / windowView.getWidth();
            canvas.scale(scale, scale);

            TextureView textureView = previewView.getTextureView();
            if (textureView == null) {
                textureView = previewView.filterTextureView;
            }
            if (textureView != null) {
                canvas.save();
                canvas.translate(containerView.getX() + previewContainer.getX(), containerView.getY() + previewContainer.getY());
                int w = (int) (textureView.getWidth() / amount), h = (int) (textureView.getHeight() / amount);
                try {
                    Bitmap textureBitmap = textureView.getBitmap(w, h);
                    canvas.scale(1f / scale, 1f / scale);
                    canvas.drawBitmap(textureBitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
                    textureBitmap.recycle();
                } catch (Exception ignore) {}
                canvas.restore();
            }
            canvas.save();
            canvas.translate(containerView.getX(), containerView.getY());
            for (int i = 0; i < containerView.getChildCount(); ++i) {
                View child = containerView.getChildAt(i);
                canvas.save();
                canvas.translate(child.getX(), child.getY());
                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                } else if (child == previewContainer) {
                    for (int j = 0; j < previewContainer.getChildCount(); ++j) {
                        child = previewContainer.getChildAt(j);
                        if (child == previewView || child == cameraView || child == cameraViewThumb || child.getVisibility() != View.VISIBLE) {
                            continue;
                        }
                        canvas.save();
                        canvas.translate(child.getX(), child.getY());
                        child.draw(canvas);
                        canvas.restore();
                    }
                } else {
                    child.draw(canvas);
                }
                canvas.restore();
            }
            canvas.restore();
        }
    }

    public void showAvatarConstructorFragment(AvatarConstructorPreviewCell view, TLRPC.VideoSize emojiMarkupStrat) {
        AvatarConstructorFragment avatarConstructorFragment = new AvatarConstructorFragment(parentAlert.parentImageUpdater, parentAlert.getAvatarFor());
        avatarConstructorFragment.finishOnDone = !(parentAlert.getAvatarFor() != null && parentAlert.getAvatarFor().type == ImageUpdater.TYPE_SUGGEST_PHOTO_FOR_USER);
        parentAlert.baseFragment.presentFragment(avatarConstructorFragment);
        if (view != null) {
            avatarConstructorFragment.startFrom(view);
        }
        if (emojiMarkupStrat != null) {
            avatarConstructorFragment.startFrom(emojiMarkupStrat);
        }
        avatarConstructorFragment.setDelegate((gradient, documentId, document, previewView) -> {
            selectedPhotos.clear();
            Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            GradientTools gradientTools = new GradientTools();
            if (gradient != null) {
                gradientTools.setColors(gradient.color1, gradient.color2, gradient.color3, gradient.color4);
            } else {
                gradientTools.setColors(AvatarConstructorFragment.defaultColors[0][0], AvatarConstructorFragment.defaultColors[0][1],  AvatarConstructorFragment.defaultColors[0][2], AvatarConstructorFragment.defaultColors[0][3]);
            }
            gradientTools.setBounds(0, 0, 800, 800);
            canvas.drawRect(0, 0, 800, 800, gradientTools.paint);

            File file = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), SharedConfig.getLastLocalId() + "avatar_background.png");
            try {
                file.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            float scale = AvatarConstructorFragment.STICKER_DEFAULT_SCALE;
            int imageX, imageY;
            imageX = imageY = (int) (800 * (1f - scale) / 2f);
            int imageSize = (int) (800 * scale);

            ImageReceiver imageReceiver = previewView.getImageReceiver();
            if (imageReceiver.getAnimation() != null) {
                Bitmap firstFrame = imageReceiver.getAnimation().getFirstFrame(null);
                ImageReceiver firstFrameReceiver = new ImageReceiver();
                firstFrameReceiver.setImageBitmap(firstFrame);
                firstFrameReceiver.setImageCoords(imageX, imageY, imageSize, imageSize);
                firstFrameReceiver.setRoundRadius((int) (imageSize * AvatarConstructorFragment.STICKER_DEFAULT_ROUND_RADIUS));
                firstFrameReceiver.draw(canvas);
                firstFrameReceiver.clearImage();
                firstFrame.recycle();
            } else {
                if (imageReceiver.getLottieAnimation() != null) {
                    imageReceiver.getLottieAnimation().setCurrentFrame(0, false, true);
                }
                imageReceiver.setImageCoords(imageX, imageY, imageSize, imageSize);
                imageReceiver.setRoundRadius((int) (imageSize * AvatarConstructorFragment.STICKER_DEFAULT_ROUND_RADIUS));
                imageReceiver.draw(canvas);
            }

            File thumb = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), SharedConfig.getLastLocalId() + "avatar_background.png");
            try {
                thumb.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(thumb);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaController.PhotoEntry photoEntry;
            if (previewView.hasAnimation()) {
                photoEntry = new MediaController.PhotoEntry(0, 0, 0, file.getPath(), 0, false, 0, 0, 0);
                photoEntry.thumbPath = thumb.getPath();

                if (previewView.documentId != 0) {
                    TLRPC.TL_videoSizeEmojiMarkup emojiMarkup = new TLRPC.TL_videoSizeEmojiMarkup();
                    emojiMarkup.emoji_id = previewView.documentId;
                    emojiMarkup.background_colors.add(previewView.backgroundGradient.color1);
                    if (previewView.backgroundGradient.color2 != 0) {
                        emojiMarkup.background_colors.add(previewView.backgroundGradient.color2);
                    }
                    if (previewView.backgroundGradient.color3 != 0) {
                        emojiMarkup.background_colors.add(previewView.backgroundGradient.color3);
                    }
                    if (previewView.backgroundGradient.color4 != 0) {
                        emojiMarkup.background_colors.add(previewView.backgroundGradient.color4);
                    }
                    photoEntry.emojiMarkup = emojiMarkup;
                } else if (previewView.document != null) {
                    TLRPC.TL_videoSizeStickerMarkup emojiMarkup = new TLRPC.TL_videoSizeStickerMarkup();
                    emojiMarkup.sticker_id = previewView.document.id;
                    emojiMarkup.stickerset = MessageObject.getInputStickerSet(previewView.document);
                    emojiMarkup.background_colors.add(previewView.backgroundGradient.color1);
                    if (previewView.backgroundGradient.color2 != 0) {
                        emojiMarkup.background_colors.add(previewView.backgroundGradient.color2);
                    }
                    if (previewView.backgroundGradient.color3 != 0) {
                        emojiMarkup.background_colors.add(previewView.backgroundGradient.color3);
                    }
                    if (previewView.backgroundGradient.color4 != 0) {
                        emojiMarkup.background_colors.add(previewView.backgroundGradient.color4);
                    }
                    photoEntry.emojiMarkup = emojiMarkup;
                }

                photoEntry.editedInfo = new VideoEditedInfo();
                photoEntry.editedInfo.originalPath = file.getPath();
                photoEntry.editedInfo.resultWidth = 800;
                photoEntry.editedInfo.resultHeight = 800;
                photoEntry.editedInfo.originalWidth = 800;
                photoEntry.editedInfo.originalHeight = 800;
                photoEntry.editedInfo.isPhoto = true;
                photoEntry.editedInfo.bitrate = -1;
                photoEntry.editedInfo.muted = true;

                photoEntry.editedInfo.start = photoEntry.editedInfo.startTime = 0;
                photoEntry.editedInfo.endTime = previewView.getDuration();
                photoEntry.editedInfo.framerate = 30;

                photoEntry.editedInfo.avatarStartTime = 0;
                photoEntry.editedInfo.estimatedSize = (int) (photoEntry.editedInfo.endTime / 1000.0f * 115200);
                photoEntry.editedInfo.estimatedDuration = photoEntry.editedInfo.endTime;

                VideoEditedInfo.MediaEntity mediaEntity = new VideoEditedInfo.MediaEntity();
                mediaEntity.type = 0;

                if (document == null) {
                    document = AnimatedEmojiDrawable.findDocument(UserConfig.selectedAccount, documentId);
                }
                if (document == null) {
                    return;
                }
                mediaEntity.viewWidth = (int) (800 * scale);
                mediaEntity.viewHeight = (int) (800 * scale);
                mediaEntity.width = scale;
                mediaEntity.height = scale;
                mediaEntity.x = (1f - scale) / 2f;
                mediaEntity.y = (1f - scale) / 2f;
                mediaEntity.document = document;
                mediaEntity.parentObject = null;
                mediaEntity.text = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(document, true).getAbsolutePath();
                mediaEntity.roundRadius = AvatarConstructorFragment.STICKER_DEFAULT_ROUND_RADIUS;
                if (MessageObject.isAnimatedStickerDocument(document, true) || MessageObject.isVideoStickerDocument(document)) {
                    boolean isAnimatedSticker = MessageObject.isAnimatedStickerDocument(document, true);
                    mediaEntity.subType |= isAnimatedSticker ? 1 : 4;
                }
                if (MessageObject.isTextColorEmoji(document)) {
                    mediaEntity.color = 0xFFFFFFFF;
                    mediaEntity.subType |= 8;
                }

                photoEntry.editedInfo.mediaEntities = new ArrayList<>();
                photoEntry.editedInfo.mediaEntities.add(mediaEntity);
            } else {
                photoEntry = new MediaController.PhotoEntry(0, 0, 0, thumb.getPath(), 0, false, 0, 0, 0);
            }
            selectedPhotos.put(-1, photoEntry);
            selectedPhotosOrder.add(-1);
            parentAlert.delegate.didPressedButton(7, true, false, 0, 0, parentAlert.isCaptionAbove(), false);
            if (!avatarConstructorFragment.finishOnDone) {
                if (parentAlert.baseFragment != null) {
                    parentAlert.baseFragment.removeSelfFromStack();
                }
                avatarConstructorFragment.finishFragment();
            }
        });
    }

    private boolean checkSendMediaEnabled(MediaController.PhotoEntry photoEntry) {
        if (!videoEnabled && photoEntry.isVideo) {
            if (parentAlert.checkCanRemoveRestrictionsByBoosts()) {
                return true;
            }
            BulletinFactory.of(parentAlert.sizeNotifierFrameLayout, resourcesProvider).createErrorBulletin(
                    LocaleController.getString(R.string.GlobalAttachVideoRestricted)
            ).show();
            return true;
        } else if (!photoEnabled && !photoEntry.isVideo) {
            if (parentAlert.checkCanRemoveRestrictionsByBoosts()) {
                return true;
            }
            BulletinFactory.of(parentAlert.sizeNotifierFrameLayout, resourcesProvider).createErrorBulletin(
                    LocaleController.getString(R.string.GlobalAttachPhotoRestricted)
            ).show();
            return true;
        }
        return false;
    }

    private int maxCount() {
        if (parentAlert.baseFragment instanceof ChatActivity && ((ChatActivity) parentAlert.baseFragment).getChatMode() == ChatActivity.MODE_QUICK_REPLIES) {
            return parentAlert.baseFragment.getMessagesController().quickReplyMessagesLimit - ((ChatActivity) parentAlert.baseFragment).messages.size();
        }
        return Integer.MAX_VALUE;
    }

    private int addToSelectedPhotos(MediaController.PhotoEntry object, int index) {
        Object key = object.imageId;
        if (selectedPhotos.containsKey(key)) {
            object.starsAmount = 0;
            object.hasSpoiler = false;

            selectedPhotos.remove(key);
            int position = selectedPhotosOrder.indexOf(key);
            if (position >= 0) {
                selectedPhotosOrder.remove(position);
            }
            updatePhotosCounter(false);
            updateCheckedPhotoIndices();
            if (index >= 0) {
                object.reset();
                photoViewerProvider.updatePhotoAtIndex(index);
            }
            return position;
        } else {
            object.starsAmount = getStarsPrice();
            object.hasSpoiler = getStarsPrice() > 0;
            object.isChatPreviewSpoilerRevealed = false;
            object.isAttachSpoilerRevealed = false;

            boolean changed = checkSelectedCount(true);
            selectedPhotos.put(key, object);
            selectedPhotosOrder.add(key);
            if (changed) {
                updateCheckedPhotos();
            } else {
                updatePhotosCounter(true);
            }
            return -1;
        }
    }

    private boolean checkSelectedCount(boolean beforeAdding) {
        boolean changed = false;
        if (getStarsPrice() > 0) {
            while (selectedPhotos.size() > 10 - (beforeAdding ? 1 : 0) && !selectedPhotosOrder.isEmpty()) {
                Object key = selectedPhotosOrder.get(0);
                Object firstPhoto = selectedPhotos.get(key);
                if (!(firstPhoto instanceof MediaController.PhotoEntry)) {
                    break;
                }
                addToSelectedPhotos((MediaController.PhotoEntry) firstPhoto, -1);
                changed = true;
            }
        }
        return changed;
    }

    public long getStarsPrice() {
        for (HashMap.Entry<Object, Object> entry : selectedPhotos.entrySet()) {
            MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry.getValue();
            return photoEntry.starsAmount;
        }
        return 0;
    }

    public void setStarsPrice(long stars) {
        if (!selectedPhotos.isEmpty()) {
            for (HashMap.Entry<Object, Object> entry : selectedPhotos.entrySet()) {
                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry.getValue();
                photoEntry.starsAmount = stars;
                photoEntry.hasSpoiler = stars > 0;
                photoEntry.isChatPreviewSpoilerRevealed = false;
                photoEntry.isAttachSpoilerRevealed = false;
            }
        }
        onSelectedItemsCountChanged(getSelectedItemsCount());
        if (checkSelectedCount(false)) {
            updateCheckedPhotos();
        }
    }

    private void updatePhotoStarsPrice() {
        gridView.forAllChild(view -> {
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                cell.setHasSpoiler(cell.getPhotoEntry() != null && cell.getPhotoEntry().hasSpoiler, 250f);
                cell.setStarsPrice(cell.getPhotoEntry() != null ? cell.getPhotoEntry().starsAmount : 0, selectedPhotos.size() > 1);
            }
        });
    }

    public void clearSelectedPhotos() {
        spoilerItem.setText(LocaleController.getString(R.string.EnablePhotoSpoiler));
        spoilerItem.setAnimatedIcon(R.raw.photo_spoiler);
        parentAlert.selectedMenuItem.showSubItem(compress);
        if (!selectedPhotos.isEmpty()) {
            for (HashMap.Entry<Object, Object> entry : selectedPhotos.entrySet()) {
                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry.getValue();
                photoEntry.reset();
            }
            selectedPhotos.clear();
            selectedPhotosOrder.clear();
        }
        if (!cameraPhotos.isEmpty()) {
            for (int a = 0, size = cameraPhotos.size(); a < size; a++) {
                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) cameraPhotos.get(a);
                new File(photoEntry.path).delete();
                if (photoEntry.imagePath != null) {
                    new File(photoEntry.imagePath).delete();
                }
                if (photoEntry.thumbPath != null) {
                    new File(photoEntry.thumbPath).delete();
                }
            }
            cameraPhotos.clear();
        }
        adapter.notifyDataSetChanged();
        cameraAttachAdapter.notifyDataSetChanged();
    }

    private void updateAlbumsDropDown() {
        dropDownContainer.removeAllSubItems();
        if (mediaEnabled) {
            ArrayList<MediaController.AlbumEntry> albums;
            if (shouldLoadAllMedia()) {
                albums = MediaController.allMediaAlbums;
            } else {
                albums = MediaController.allPhotoAlbums;
            }
            dropDownAlbums = new ArrayList<>(albums);
            Collections.sort(dropDownAlbums, (o1, o2) -> {
                if (o1.bucketId == 0 && o2.bucketId != 0) {
                    return -1;
                } else if (o1.bucketId != 0 && o2.bucketId == 0) {
                    return 1;
                }
                int index1 = albums.indexOf(o1);
                int index2 = albums.indexOf(o2);
                if (index1 > index2) {
                    return 1;
                } else if (index1 < index2) {
                    return -1;
                } else {
                    return 0;
                }

            });
        } else {
            dropDownAlbums = new ArrayList<>();
        }
        if (dropDownAlbums.isEmpty()) {
            dropDown.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            dropDown.setCompoundDrawablesWithIntrinsicBounds(null, null, dropDownDrawable, null);
            for (int a = 0, N = dropDownAlbums.size(); a < N; a++) {
                MediaController.AlbumEntry album = dropDownAlbums.get(a);
                AlbumButton btn = new AlbumButton(getContext(), album.coverPhoto, album.bucketName, album.photos.size(), resourcesProvider);
                dropDownContainer.getPopupLayout().addView(btn);
                final int i = a + 10;
                btn.setOnClickListener(v -> {
                    parentAlert.actionBar.getActionBarMenuOnItemClick().onItemClick(i);
                    dropDownContainer.toggleSubMenu();
                });
            }
        }
    }

    private boolean processTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (!pressed && event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            zoomControlView.getHitRect(hitRect);
            if (zoomControlView.getTag() != null && hitRect.contains((int) event.getX(), (int) event.getY())) {
                return false;
            }
            if (!takingPhoto && !dragging) {
                if (event.getPointerCount() == 2) {
                    pinchStartDistance = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0));
                    zooming = true;
                } else {
                    maybeStartDraging = true;
                    lastY = event.getY();
                    zooming = false;
                }
                zoomWas = false;
                pressed = true;
            }
        } else if (pressed) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if (zooming && event.getPointerCount() == 2 && !dragging) {
                    float newDistance = (float) Math.hypot(event.getX(1) - event.getX(0), event.getY(1) - event.getY(0));
                    if (!zoomWas) {
                        if (Math.abs(newDistance - pinchStartDistance) >= AndroidUtilities.getPixelsInCM(0.4f, false)) {
                            pinchStartDistance = newDistance;
                            zoomWas = true;
                        }
                    } else {
                        if (cameraView != null) {
                            float diff = (newDistance - pinchStartDistance) / dp(100);
                            pinchStartDistance = newDistance;
                            cameraZoom += diff;
                            if (cameraZoom < 0.0f) {
                                cameraZoom = 0.0f;
                            } else if (cameraZoom > 1.0f) {
                                cameraZoom = 1.0f;
                            }
                            zoomControlView.setZoom(cameraZoom, false);
                            parentAlert.getSheetContainer().invalidate();
                            cameraView.setZoom(cameraZoom);
                            showZoomControls(true, true);
                        }
                    }
                } else {
                    float newY = event.getY();
                    float dy = (newY - lastY);
                    if (maybeStartDraging) {
                        if (Math.abs(dy) > AndroidUtilities.getPixelsInCM(0.4f, false)) {
                            maybeStartDraging = false;
                            dragging = true;
                        }
                    } else if (dragging) {
                        if (cameraView != null) {
                            cameraView.setTranslationY(cameraView.getTranslationY() + dy);
                            lastY = newY;
                            zoomControlView.setTag(null);
                            if (zoomControlHideRunnable != null) {
                                AndroidUtilities.cancelRunOnUIThread(zoomControlHideRunnable);
                                zoomControlHideRunnable = null;
                            }
                            if (windowView.getTag() == null) {
                                windowView.setTag(1);
                                AnimatorSet animatorSet = new AnimatorSet();
                                animatorSet.playTogether(
                                        ObjectAnimator.ofFloat(windowView, View.ALPHA, 0.0f),
                                        ObjectAnimator.ofFloat(zoomControlView, View.ALPHA, 0.0f),
                                        ObjectAnimator.ofFloat(counterTextView, View.ALPHA, 0.0f),
                                        ObjectAnimator.ofFloat(cameraPhotoRecyclerView, View.ALPHA, 0.0f));
                                animatorSet.setDuration(220);
                                animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
                                animatorSet.start();
                            }
                        }
                    }
                }
            } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL || event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                pressed = false;
                zooming = false;
                if (zooming) {
                    zooming = false;
                } else if (dragging) {
                    dragging = false;
                    if (cameraView != null) {
                        if (Math.abs(cameraView.getTranslationY()) > cameraView.getMeasuredHeight() / 6.0f) {
                            closeCamera(true);
                        } else {
                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.playTogether(
                                    ObjectAnimator.ofFloat(cameraView, View.TRANSLATION_Y, 0.0f),
                                    ObjectAnimator.ofFloat(windowView, View.ALPHA, 1.0f),
                                    ObjectAnimator.ofFloat(counterTextView, View.ALPHA, 1.0f),
                                    ObjectAnimator.ofFloat(cameraPhotoRecyclerView, View.ALPHA, 1.0f));
                            animatorSet.setDuration(250);
                            animatorSet.setInterpolator(interpolator);
                            animatorSet.start();
                            windowView.setTag(null);
                        }
                    }
                } else if (cameraView != null && !zoomWas) {
                    cameraView.getLocationOnScreen(viewPosition);
                    float viewX = event.getRawX() - viewPosition[0];
                    float viewY = event.getRawY() - viewPosition[1];
                    cameraView.focusToPoint((int) viewX, (int) viewY);
                }
            }
        }
        return true;
    }

    private void resetRecordState() {
        if (parentAlert.destroyed) {
            return;
        }
        switchCameraButton.animate().alpha(1f).translationX(0).setDuration(150).setInterpolator(CubicBezierInterpolator.DEFAULT).start();
        tooltipTextView.animate().alpha(1f).setDuration(150).setInterpolator(CubicBezierInterpolator.DEFAULT).start();
        AndroidUtilities.updateViewVisibilityAnimated(recordTime, false);

        AndroidUtilities.cancelRunOnUIThread(videoRecordRunnable);
        videoRecordRunnable = null;
        AndroidUtilities.unlockOrientation(AndroidUtilities.findActivity(getContext()));
    }

    protected void openPhotoViewer(MediaController.PhotoEntry entry, final boolean sameTakePictureOrientation, boolean external) {
        if (entry != null) {
            cameraPhotos.add(entry);
            selectedPhotos.put(entry.imageId, entry);
            selectedPhotosOrder.add(entry.imageId);
            parentAlert.updateCountButton(0);
            adapter.notifyDataSetChanged();
            cameraAttachAdapter.notifyDataSetChanged();
        }
        if (entry != null && !external && cameraPhotos.size() > 1) {
            updatePhotosCounter(false);
            if (cameraView != null) {
                zoomControlView.setZoom(0.0f, false);
                cameraZoom = 0.0f;
                cameraView.setZoom(0.0f);
                CameraController.getInstance().startPreview(cameraView.getCameraSessionObject());
            }
            return;
        }
        if (cameraPhotos.isEmpty()) {
            return;
        }
        cancelTakingPhotos = true;

        BaseFragment fragment = parentAlert.baseFragment;
        if (fragment == null) {
            fragment = LaunchActivity.getLastFragment();
        }
        if (fragment == null) {
            return;
        }
        PhotoViewer.getInstance().setParentActivity(fragment.getParentActivity(), resourcesProvider);
        PhotoViewer.getInstance().setParentAlert(parentAlert);
        PhotoViewer.getInstance().setMaxSelectedPhotos(parentAlert.maxSelectedPhotos, parentAlert.allowOrder);

        ChatActivity chatActivity;
        int type;
        if (parentAlert.isPhotoPicker && parentAlert.isStickerMode) {
            type = PhotoViewer.SELECT_TYPE_STICKER;
            chatActivity = (ChatActivity) parentAlert.baseFragment;
        } else if (parentAlert.avatarPicker != 0) {
            type = PhotoViewer.SELECT_TYPE_AVATAR;
            chatActivity = null;
        } else if (parentAlert.baseFragment instanceof ChatActivity) {
            chatActivity = (ChatActivity) parentAlert.baseFragment;
            type = 2;
        } else {
            chatActivity = null;
            type = 5;
        }
        ArrayList<Object> arrayList;
        int index;
        if (parentAlert.avatarPicker != 0) {
            arrayList = new ArrayList<>();
            arrayList.add(entry);
            index = 0;
        } else {
            arrayList = getAllPhotosArray();
            index = cameraPhotos.size() - 1;
        }
        if (parentAlert.getAvatarFor() != null && entry != null) {
            parentAlert.getAvatarFor().isVideo = entry.isVideo;
        }
        PhotoViewer.getInstance().openPhotoForSelect(arrayList, index, type, false, new BasePhotoProvider() {

            @Override
            public void onOpen() {
                pauseCameraPreview();
            }

            @Override
            public void onClose() {
                resumeCameraPreview();
            }

            public void onEditModeChanged(boolean isEditMode) {
                onPhotoEditModeChanged(isEditMode);
            }

            @Override
            public ImageReceiver.BitmapHolder getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
                return null;
            }

            @Override
            public boolean cancelButtonPressed() {
                if (cameraOpened && cameraView != null) {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (cameraView != null && !parentAlert.isDismissed() && Build.VERSION.SDK_INT >= 21) {
                            cameraView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
                        }
                    }, 1000);
                    zoomControlView.setZoom(0.0f, false);
                    cameraZoom = 0.0f;
                    cameraView.setZoom(0.0f);
                    CameraController.getInstance().startPreview(cameraView.getCameraSession());
                }
                if (cancelTakingPhotos && cameraPhotos.size() == 1) {
                    for (int a = 0, size = cameraPhotos.size(); a < size; a++) {
                        MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) cameraPhotos.get(a);
                        new File(photoEntry.path).delete();
                        if (photoEntry.imagePath != null) {
                            new File(photoEntry.imagePath).delete();
                        }
                        if (photoEntry.thumbPath != null) {
                            new File(photoEntry.thumbPath).delete();
                        }
                    }
                    cameraPhotos.clear();
                    selectedPhotosOrder.clear();
                    selectedPhotos.clear();
                    counterTextView.setVisibility(View.INVISIBLE);
                    cameraPhotoRecyclerView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    cameraAttachAdapter.notifyDataSetChanged();
                    parentAlert.updateCountButton(0);
                }
                return true;
            }

            @Override
            public void needAddMorePhotos() {
                cancelTakingPhotos = false;
                if (mediaFromExternalCamera) {
                    parentAlert.delegate.didPressedButton(0, true, true, 0, 0, parentAlert.isCaptionAbove(), false);
                    return;
                }
                if (!cameraOpened) {
                    openCamera(false);
                }
                counterTextView.setVisibility(View.VISIBLE);
                cameraPhotoRecyclerView.setVisibility(View.VISIBLE);
                counterTextView.setAlpha(1.0f);
                updatePhotosCounter(false);
            }

            @Override
            public void sendButtonPressed(int index, VideoEditedInfo videoEditedInfo, boolean notify, int scheduleDate, boolean forceDocument) {
                parentAlert.sent = true;
                if (cameraPhotos.isEmpty() || parentAlert.destroyed) {
                    return;
                }
                if (videoEditedInfo != null && index >= 0 && index < cameraPhotos.size()) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) cameraPhotos.get(index);
                    photoEntry.editedInfo = videoEditedInfo;
                }
                if (!(parentAlert.baseFragment instanceof ChatActivity) || !((ChatActivity) parentAlert.baseFragment).isSecretChat()) {
                    for (int a = 0, size = cameraPhotos.size(); a < size; a++) {
                        MediaController.PhotoEntry entry = (MediaController.PhotoEntry) cameraPhotos.get(a);
                        if (entry.ttl > 0) {
                            continue;
                        }
                        AndroidUtilities.addMediaToGallery(entry.path);
                    }
                }
                parentAlert.applyCaption();
                closeCamera(false);
                parentAlert.delegate.didPressedButton(forceDocument ? 4 : 8, true, notify, scheduleDate, 0, parentAlert.isCaptionAbove(), forceDocument);
                cameraPhotos.clear();
                selectedPhotosOrder.clear();
                selectedPhotos.clear();
                adapter.notifyDataSetChanged();
                cameraAttachAdapter.notifyDataSetChanged();
                parentAlert.dismiss(true);
            }

            @Override
            public boolean scaleToFill() {
                if (parentAlert.destroyed) {
                    return false;
                }
                int locked = Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                return sameTakePictureOrientation || locked == 1;
            }

            @Override
            public void willHidePhotoViewer() {
                int count = gridView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View view = gridView.getChildAt(a);
                    if (view instanceof PhotoAttachPhotoCell) {
                        PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                        cell.showImage();
                        cell.showCheck(true);
                    }
                }
            }

            @Override
            public boolean canScrollAway() {
                return false;
            }

            @Override
            public boolean canCaptureMorePhotos() {
                return parentAlert.maxSelectedPhotos != 1;
            }

            @Override
            public boolean allowCaption() {
                return !parentAlert.isPhotoPicker;
            }
        }, chatActivity);
        PhotoViewer.getInstance().setAvatarFor(parentAlert.getAvatarFor());
        if (parentAlert.isStickerMode) {
            PhotoViewer.getInstance().enableStickerMode(null, false, parentAlert.customStickerHandler);
            PhotoViewer.getInstance().prepareSegmentImage();
        }
    }

    private boolean inCheck() {
        final float collageProgress = collageLayoutView.hasLayout() ? collageLayoutView.getFilledProgress() : 0.0f;
        return !animatedRecording && collageProgress >= 1.0f;
    }

    private void showZoomControls(boolean show, boolean animated) {
        if (zoomControlView.getTag() != null && show || zoomControlView.getTag() == null && !show) {
            if (show) {
                if (zoomControlHideRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(zoomControlHideRunnable);
                }
                AndroidUtilities.runOnUIThread(zoomControlHideRunnable = () -> {
                    showZoomControls(false, true);
                    zoomControlHideRunnable = null;
                }, 2000);
            }
            return;
        }
        if (zoomControlAnimation != null) {
            zoomControlAnimation.cancel();
        }
        zoomControlView.setTag(show ? 1 : null);
        zoomControlAnimation = new AnimatorSet();
        zoomControlAnimation.setDuration(180);
        zoomControlAnimation.playTogether(ObjectAnimator.ofFloat(zoomControlView, View.ALPHA, show ? 1.0f : 0.0f));
        zoomControlAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                zoomControlAnimation = null;
            }
        });
        zoomControlAnimation.start();
        if (show) {
            AndroidUtilities.runOnUIThread(zoomControlHideRunnable = () -> {
                showZoomControls(false, true);
                zoomControlHideRunnable = null;
            }, 2000);
        }
    }

    protected void updatePhotosCounter(boolean added) {
        if (counterTextView == null || parentAlert.avatarPicker != 0 || parentAlert.storyMediaPicker) {
            return;
        }
        boolean hasVideo = false;
        boolean hasPhotos = false;
        for (HashMap.Entry<Object, Object> entry : selectedPhotos.entrySet()) {
            MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry.getValue();
            if (photoEntry.isVideo) {
                hasVideo = true;
            } else {
                hasPhotos = true;
            }
            if (hasVideo && hasPhotos) {
                break;
            }
        }
        int newSelectedCount = Math.max(1, selectedPhotos.size());
        if (hasVideo && hasPhotos) {
            counterTextView.setText(LocaleController.formatPluralString("Media", selectedPhotos.size()).toUpperCase());
            if (newSelectedCount != currentSelectedCount || added) {
                parentAlert.selectedTextView.setText(LocaleController.formatPluralString("MediaSelected", newSelectedCount));
            }
        } else if (hasVideo) {
            counterTextView.setText(LocaleController.formatPluralString("Videos", selectedPhotos.size()).toUpperCase());
            if (newSelectedCount != currentSelectedCount || added) {
                parentAlert.selectedTextView.setText(LocaleController.formatPluralString("VideosSelected", newSelectedCount));
            }
        } else {
            counterTextView.setText(LocaleController.formatPluralString("Photos", selectedPhotos.size()).toUpperCase());
            if (newSelectedCount != currentSelectedCount || added) {
                parentAlert.selectedTextView.setText(LocaleController.formatPluralString("PhotosSelected", newSelectedCount));
            }
        }
        parentAlert.setCanOpenPreview(newSelectedCount > 1);
        currentSelectedCount = newSelectedCount;
    }

    private PhotoAttachPhotoCell getCellForIndex(int index) {
        int count = gridView.getChildCount();
        for (int a = 0; a < count; a++) {
            View view = gridView.getChildAt(a);
            if (view.getTop() >= gridView.getMeasuredHeight() - parentAlert.getClipLayoutBottom()) {
                continue;
            }
            if (view instanceof PhotoAttachPhotoCell) {
                PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) view;
                if (cell.getImageView().getTag() != null && (Integer) cell.getImageView().getTag() == index) {
                    return cell;
                }
            }
        }
        return null;
    }

    private String flashButtonMode;
    private void setCameraFlashModeIcon(String mode, boolean animated) {
        flashButton.clearAnimation();
        if (cameraView != null && cameraView.isDual() || animatedRecording) {
            mode = null;
        }
        flashButtonMode = mode;
        if (mode == null) {
            setActionBarButtonVisible(flashButton, false, animated);
            return;
        }
        final int resId;
        switch (mode) {
            case Camera.Parameters.FLASH_MODE_ON:
                resId = R.drawable.media_photo_flash_on2;
                flashButton.setContentDescription(getString(R.string.AccDescrCameraFlashOn));
                break;
            case Camera.Parameters.FLASH_MODE_AUTO:
                resId = R.drawable.media_photo_flash_auto2;
                flashButton.setContentDescription(getString(R.string.AccDescrCameraFlashAuto));
                break;
            default:
            case Camera.Parameters.FLASH_MODE_OFF:
                resId = R.drawable.media_photo_flash_off2;
                flashButton.setContentDescription(getString(R.string.AccDescrCameraFlashOff));
                break;
        }
        flashButton.setIcon(flashButtonResId = resId, animated && flashButtonResId != resId);
        setActionBarButtonVisible(flashButton, currentPage == PAGE_CAMERA && !collageListView.isVisible() && flashButtonMode != null && !inCheck(), animated);
    }

    private void setAwakeLock(boolean lock) {
        if (lock) {
            windowLayoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            windowLayoutParams.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }
        try {
            windowManager.updateViewLayout(windowView, windowLayoutParams);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private DraftSavedHint getDraftSavedHint() {
        if (draftSavedHint == null) {
            draftSavedHint = new DraftSavedHint(getContext());
            controlContainer.addView(draftSavedHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0, 0, 66 + 12));
        }
        return draftSavedHint;
    }

    private void destroyPhotoPaintView() {
        if (paintView == null) {
            return;
        }
        paintView.onCleanupEntities();

        paintView.shutdown();
        containerView.removeView(paintView);
        paintView = null;
        if (paintViewRenderView != null) {
            previewContainer.removeView(paintViewRenderView);
            paintViewRenderView = null;
        }
        if (paintViewTextDim != null) {
            previewContainer.removeView(paintViewTextDim);
            paintViewTextDim = null;
        }
        if (paintViewRenderInputView != null) {
            previewContainer.removeView(paintViewRenderInputView);
            paintViewRenderInputView = null;
        }
        if (paintViewEntitiesView != null) {
            previewContainer.removeView(paintViewEntitiesView);
            paintViewEntitiesView = null;
        }
        if (paintViewSelectionContainerView != null) {
            previewContainer.removeView(paintViewSelectionContainerView);
            paintViewSelectionContainerView = null;
        }
    }

    private void destroyGalleryListView() {
        if (galleryListView == null) {
            return;
        }
        windowView.removeView(galleryListView);
        galleryListView = null;
        if (galleryOpenCloseAnimator != null) {
            galleryOpenCloseAnimator.cancel();
            galleryOpenCloseAnimator = null;
        }
        if (galleryOpenCloseSpringAnimator != null) {
            galleryOpenCloseSpringAnimator.cancel();
            galleryOpenCloseSpringAnimator = null;
        }
        galleryListViewOpening = null;
    }

    private void destroyPhotoFilterView() {
        if (photoFilterView == null) {
            return;
        }
        photoFilterView.shutdown();
        photoFilterEnhanceView.setFilterView(null);
        containerView.removeView(photoFilterView);
        if (photoFilterViewTextureView != null) {
            previewContainer.removeView(photoFilterViewTextureView);
            photoFilterViewTextureView = null;
        }
        previewView.setFilterTextureView(null, null);
        if (photoFilterViewBlurControl != null) {
            previewContainer.removeView(photoFilterViewBlurControl);
            photoFilterViewBlurControl = null;
        }
        if (photoFilterViewCurvesControl != null) {
            previewContainer.removeView(photoFilterViewCurvesControl);
            photoFilterViewCurvesControl = null;
        }
        photoFilterView = null;
    }

    private AnimatorSet recordingAnimator;
    private boolean animatedRecording;
    private boolean animatedRecordingWasInCheck;
    private void animateRecording(boolean recording, boolean animated) {
        if (recording) {
            if (dualHint != null) {
                dualHint.hide();
            }
            if (savedDualHint != null) {
                savedDualHint.hide();
            }
            if (muteHint != null) {
                muteHint.hide();
            }
            if (cameraHint != null) {
                cameraHint.hide();
            }
        }
        if (animatedRecording == recording && animatedRecordingWasInCheck == inCheck()) {
            return;
        }
        if (recordingAnimator != null) {
            recordingAnimator.cancel();
            recordingAnimator = null;
        }
        animatedRecording = recording;
        animatedRecordingWasInCheck = inCheck();
        if (recording && collageListView != null && collageListView.isVisible()) {
            collageListView.setVisible(false, animated);
        }
        updateActionBarButtons(animated);
        if (animated) {
            recordingAnimator = new AnimatorSet();
            recordingAnimator.playTogether(
                    ObjectAnimator.ofFloat(hintTextView, View.ALPHA, recording && currentPage == PAGE_CAMERA && !inCheck() ? 1 : 0),
                    ObjectAnimator.ofFloat(hintTextView, View.TRANSLATION_Y, recording && currentPage == PAGE_CAMERA && !inCheck() ? 0 : dp(16)),
                    ObjectAnimator.ofFloat(collageHintTextView, View.ALPHA, !recording && currentPage == PAGE_CAMERA && inCheck() ? 0.6f : 0),
                    ObjectAnimator.ofFloat(collageHintTextView, View.TRANSLATION_Y, !recording && currentPage == PAGE_CAMERA && inCheck() ? 0 : dp(16)),
                    ObjectAnimator.ofFloat(modeSwitcherView, View.ALPHA, recording || currentPage != PAGE_CAMERA || inCheck() ? 0 : 1),
                    ObjectAnimator.ofFloat(modeSwitcherView, View.TRANSLATION_Y, recording || currentPage != PAGE_CAMERA || inCheck() ? dp(16) : 0)
            );
            recordingAnimator.setDuration(260);
            recordingAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            recordingAnimator.start();
        } else {
            hintTextView.setAlpha(recording && currentPage == PAGE_CAMERA && !inCheck() ? 1f : 0);
            hintTextView.setTranslationY(recording && currentPage == PAGE_CAMERA && !inCheck() ? 0 : dp(16));
            collageHintTextView.setAlpha(!recording && currentPage == PAGE_CAMERA && inCheck() ? 0.6f : 0);
            collageHintTextView.setTranslationY(!recording && currentPage == PAGE_CAMERA && inCheck() ? 0 : dp(16));
            modeSwitcherView.setAlpha(recording || currentPage != PAGE_CAMERA || inCheck() ? 0 : 1f);
            modeSwitcherView.setTranslationY(recording || currentPage != PAGE_CAMERA || inCheck() ? dp(16) : 0);
        }
    }

    public void setActionBarButtonVisible(View view, boolean visible, boolean animated) {
        if (view == null) return;
        if (animated) {
            view.setVisibility(View.VISIBLE);
            view.animate()
                    .alpha(visible ? 1.0f : 0.0f)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                            updateActionBarButtonsOffsets();
                        }
                    })
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            updateActionBarButtonsOffsets();
                            if (!visible) {
                                view.setVisibility(View.GONE);
                            }
                        }
                    })
                    .setDuration(320)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                    .start();
        } else {
            view.animate().cancel();
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
            view.setAlpha(visible ? 1.0f : 0.0f);
            updateActionBarButtonsOffsets();
        }
    }

    public void checkCamera(boolean request) {
        if (parentAlert.destroyed || !needCamera) {
            return;
        }
        boolean old = deviceHasGoodCamera;
        boolean old2 = noCameraPermissions;
        BaseFragment fragment = parentAlert.baseFragment;
        if (fragment == null) {
            fragment = LaunchActivity.getLastFragment();
        }
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        if (!SharedConfig.inappCamera) {
            deviceHasGoodCamera = false;
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (noCameraPermissions = (fragment.getParentActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                    if (request) {
                        try {
                            parentAlert.baseFragment.getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 17);
                        } catch (Exception ignore) {

                        }
                    }
                    deviceHasGoodCamera = false;
                } else {
                    if (request || SharedConfig.hasCameraCache) {
                        CameraController.getInstance().initCamera(null);
                    }
                    deviceHasGoodCamera = CameraController.getInstance().isCameraInitied();
                }
            } else {
                if (request || SharedConfig.hasCameraCache) {
                    CameraController.getInstance().initCamera(null);
                }
                deviceHasGoodCamera = CameraController.getInstance().isCameraInitied();
            }
        }
        if ((old != deviceHasGoodCamera || old2 != noCameraPermissions) && adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (!parentAlert.destroyed && parentAlert.isShowing() && deviceHasGoodCamera && parentAlert.getBackDrawable().getAlpha() != 0 && !cameraOpened) {
            showCamera();
        }
    }

    boolean cameraExpanded;
    private void openCamera(boolean animated) {
        if (cameraView == null || cameraInitAnimation != null || parentAlert.isDismissed()) {
            return;
        }
        cameraView.initTexture();
        if (shouldLoadAllMedia()) {
            tooltipTextView.setVisibility(VISIBLE);
        } else {
            tooltipTextView.setVisibility(GONE);
        }
        if (cameraPhotos.isEmpty()) {
            counterTextView.setVisibility(View.INVISIBLE);
            cameraPhotoRecyclerView.setVisibility(View.GONE);
        } else {
            counterTextView.setVisibility(View.VISIBLE);
            cameraPhotoRecyclerView.setVisibility(View.VISIBLE);
        }
        if (parentAlert.getCommentView().isKeyboardVisible() && isFocusable()) {
            parentAlert.getCommentView().closeKeyboard();
        }
        zoomControlView.setVisibility(View.VISIBLE);
        zoomControlView.setAlpha(0.0f);
        windowView.setVisibility(View.VISIBLE);
        windowView.setTag(null);
        cameraPanel.setVisibility(View.VISIBLE);
        cameraPanel.setAlpha(1.0f);
        cameraPanel.setTag(null);
        animateCameraValues[0] = 0;
        animateCameraValues[1] = itemSize;
        animateCameraValues[2] = itemSize;
        additionCloseCameraY = 0;
        cameraExpanded = true;
        if (cameraView != null) {
            cameraView.setFpsLimit(-1);
        }
        AndroidUtilities.hideKeyboard(this);
        AndroidUtilities.setLightNavigationBar(parentAlert.getWindow(), false);
        parentAlert.getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        if (animated) {
            setCameraOpenProgress(0);
            cameraAnimationInProgress = true;
            notificationsLocker.lock();
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(ObjectAnimator.ofFloat(this, "cameraOpenProgress", 0.0f, 1.0f));
            animators.add(ObjectAnimator.ofFloat(windowView, View.ALPHA, 1.0f));
            animators.add(ObjectAnimator.ofFloat(counterTextView, View.ALPHA, 1.0f));
            animators.add(ObjectAnimator.ofFloat(cameraPhotoRecyclerView, View.ALPHA, 1.0f));
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.setDuration(350);
            animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    notificationsLocker.unlock();
                    cameraAnimationInProgress = false;
                    if (cameraView != null) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            cameraView.invalidateOutline();
                        } else {
                            cameraView.invalidate();
                        }
                    }
                    if (cameraOpened) {
                        parentAlert.delegate.onCameraOpened();
                    }
                    if (Build.VERSION.SDK_INT >= 21 && cameraView != null) {
                        windowView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    }
                }
            });
            animatorSet.start();
        } else {
            setCameraOpenProgress(1.0f);
            windowView.setAlpha(1.0f);
            counterTextView.setAlpha(1.0f);
            cameraPhotoRecyclerView.setAlpha(1.0f);
            parentAlert.delegate.onCameraOpened();
            if (cameraView != null && Build.VERSION.SDK_INT >= 21) {
                windowView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
        cameraOpened = true;
        if (cameraView != null) {
            cameraView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            gridView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }

        if (!LiteMode.isEnabled(LiteMode.FLAGS_CHAT) && cameraView != null && cameraView.isInited()) {
            cameraView.showTexture(true, animated);
        }
        AndroidUtilities.lockOrientation(parentAlert.baseFragment.getParentActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void loadGalleryPhotos() {
        MediaController.AlbumEntry albumEntry;
        if (shouldLoadAllMedia()) {
            albumEntry = MediaController.allMediaAlbumEntry;
        } else {
            albumEntry = MediaController.allPhotosAlbumEntry;
        }
        if (albumEntry == null && Build.VERSION.SDK_INT >= 21) {
            MediaController.loadGalleryPhotosAlbums(0);
        }
    }

    private boolean shouldLoadAllMedia() {
        return !parentAlert.isPhotoPicker && (parentAlert.baseFragment instanceof ChatActivity || parentAlert.storyMediaPicker || parentAlert.avatarPicker == 2);
    }

    public void showCamera() {
        if (parentAlert.paused || !mediaEnabled) {
            return;
        }
        if (cameraView == null) {
            final boolean lazy = !LiteMode.isEnabled(LiteMode.FLAGS_CHAT);
            cameraView = new DualCameraView(getContext(), isCameraFrontfaceBeforeEnteringEditMode != null ? isCameraFrontfaceBeforeEnteringEditMode : parentAlert.openWithFrontFaceCamera, lazy) {

                Bulletin.Delegate bulletinDelegate = new Bulletin.Delegate() {
                    @Override
                    public int getBottomOffset(int tag) {
                        return dp(126) + parentAlert.getBottomInset();
                    }
                };

                @Override
                protected void dispatchDraw(Canvas canvas) {
                    if (AndroidUtilities.makingGlobalBlurBitmap) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 21) {
                        super.dispatchDraw(canvas);
                    } else {
                        int maxY = (int) Math.min(parentAlert.getCommentTextViewTop() + currentPanTranslationY + parentAlert.getContainerView().getTranslationY() - cameraView.getTranslationY() - (parentAlert.mentionContainer != null ? parentAlert.mentionContainer.clipBottom() + dp(8) : 0), getMeasuredHeight());
                        if (cameraAnimationInProgress) {
                            AndroidUtilities.rectTmp.set(animationClipLeft + cameraViewOffsetX * (1f - cameraOpenProgress), animationClipTop + cameraViewOffsetY * (1f - cameraOpenProgress), animationClipRight, Math.min(maxY, animationClipBottom));
                        } else if (!cameraAnimationInProgress && !cameraOpened) {
                            AndroidUtilities.rectTmp.set(cameraViewOffsetX, cameraViewOffsetY, getMeasuredWidth(), Math.min(maxY, getMeasuredHeight()));
                        } else {
                            AndroidUtilities.rectTmp.set(0, 0, getMeasuredWidth(), Math.min(maxY, getMeasuredHeight()));
                        }
                        canvas.save();
                        canvas.clipRect(AndroidUtilities.rectTmp);
                        super.dispatchDraw(canvas);
                        canvas.restore();
                    }
                }

                @Override
                public void onEntityDraggedTop(boolean value) {
                    previewHighlight.show(true, value, actionBarContainer);
                }

                @Override
                public void onEntityDraggedBottom(boolean value) {
                    previewHighlight.updateCaption(captionEdit.getText());
                    previewHighlight.show(false, value, controlContainer);
                }

                @Override
                public void toggleDual() {
                    super.toggleDual();
                    dualButton.setValue(isDual());
                    setCameraFlashModeIcon(getCurrentFlashMode(), true);
                }

                @Override
                protected void onSavedDualCameraSuccess() {
                    if (MessagesController.getGlobalMainSettings().getInt("storysvddualhint", 0) < 2) {
                        AndroidUtilities.runOnUIThread(() -> {
                            if (takingVideo || takingPhoto || cameraView == null || currentPage != PAGE_CAMERA) {
                                return;
                            }
                            if (savedDualHint != null) {
                                CharSequence text = isFrontface() ? getString(R.string.StoryCameraSavedDualBackHint) : getString(R.string.StoryCameraSavedDualFrontHint);
                                savedDualHint.setMaxWidthPx(HintView2.cutInFancyHalf(text, savedDualHint.getTextPaint()));
                                savedDualHint.setText(text);
                                savedDualHint.show();
                                MessagesController.getGlobalMainSettings().edit().putInt("storysvddualhint", MessagesController.getGlobalMainSettings().getInt("storysvddualhint", 0) + 1).apply();
                            }
                        }, 340);
                    }
                    dualButton.setValue(isDual());
                }

                @Override
                protected void receivedAmplitude(double amplitude) {
                    if (recordControl != null) {
                        recordControl.setAmplitude(Utilities.clamp((float) (amplitude / WaveDrawable.MAX_AMPLITUDE), 1, 0), true);
                    }
                }

                @Override
                protected void onAttachedToWindow() {
                    super.onAttachedToWindow();
                    Bulletin.addDelegate(cameraView, bulletinDelegate);
                }

                @Override
                protected void onDetachedFromWindow() {
                    super.onDetachedFromWindow();
                    Bulletin.removeDelegate(cameraView);
                }
            };

            // Existing configurations
            cameraView.fromChatAttachAlertPhotoLayout = true;
            if (cameraCell != null && lazy) {
                cameraView.setThumbDrawable(cameraCell.getDrawable());
            }
            cameraView.setRecordFile(AndroidUtilities.generateVideoPath(parentAlert.baseFragment instanceof ChatActivity && ((ChatActivity) parentAlert.baseFragment).isSecretChat()));
            cameraView.setFocusable(true);
            cameraView.setFpsLimit(30);

            // Handle dual-camera availability
            setActionBarButtonVisible(dualButton, cameraView.dualAvailable(), true);

            parentAlert.getContainer().addView(windowView, 1, new FrameLayout.LayoutParams(itemSize, itemSize));
//            parentAlert.getContainer().addView(cameraView, 1, new FrameLayout.LayoutParams(itemSize, itemSize));
            // Flash mode setup
            cameraView.setDelegate(() -> {
                String currentFlashMode = getCurrentFlashMode();
                if (TextUtils.equals(currentFlashMode, getNextFlashMode())) {
                    currentFlashMode = null;
                }
                setCameraFlashModeIcon(currentPage == PAGE_CAMERA ? currentFlashMode : null, true);
            });

            // Remaining logic (UI elements, animations, etc.)
            if (cameraIcon == null) {
                cameraIcon = new FrameLayout(getContext()) {
                    @Override
                    protected void onDraw(Canvas canvas) {
                        // Existing draw logic...
                    }
                };
                cameraIcon.setWillNotDraw(false);
                cameraIcon.setClipChildren(true);
            }

            parentAlert.getContainer().addView(cameraIcon, 2, new FrameLayout.LayoutParams(itemSize, itemSize));

            // Visibility and animation setup
            cameraView.setAlpha(mediaEnabled ? 1.0f : 0.2f);
            cameraView.setEnabled(mediaEnabled);
            cameraIcon.setAlpha(mediaEnabled ? 1.0f : 0.2f);
            cameraIcon.setEnabled(mediaEnabled);

            if (isHidden) {
                cameraView.setVisibility(GONE);
                cameraIcon.setVisibility(GONE);
            }
            if (cameraOpened) {
                cameraIcon.setAlpha(0f);
            } else {
                checkCameraViewPosition();
            }
            if (recordControl != null) {
                recordControl.setAmplitude(0, false);
            }
            cameraView.recordHevc = !collageLayoutView.hasLayout();
            cameraView.setThumbDrawable(getCameraThumb());
            cameraView.initTexture();
            cameraView.setDelegate(() -> {
                String currentFlashMode = getCurrentFlashMode();
                if (TextUtils.equals(currentFlashMode, getNextFlashMode())) {
                    currentFlashMode = null;
                }
                setCameraFlashModeIcon(currentPage == PAGE_CAMERA ? currentFlashMode : null, true);
                if (zoomControlView != null) {
                    zoomControlView.setZoom(cameraZoom = 0, false);
                }
                updateActionBarButtons(true);
            });
            setActionBarButtonVisible(dualButton, cameraView.dualAvailable() && currentPage == PAGE_CAMERA, true);
            collageButton.setTranslationX(cameraView.dualAvailable() ? 0 : dp(46));
//        collageLayoutView.getLast().addView(cameraView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.FILL));
            collageLayoutView.setCameraView(cameraView);
            if (MessagesController.getGlobalMainSettings().getInt("storyhint2", 0) < 1) {
                cameraHint.show();
                MessagesController.getGlobalMainSettings().edit().putInt("storyhint2", MessagesController.getGlobalMainSettings().getInt("storyhint2", 0) + 1).apply();
            } else if (!cameraView.isSavedDual() && cameraView.dualAvailable() && MessagesController.getGlobalMainSettings().getInt("storydualhint", 0) < 2) {
                dualHint.show();
            }

            invalidate();
        }
    }


    public void hideCamera(boolean async) {
        if (!deviceHasGoodCamera || cameraView == null) {
            return;
        }
        AndroidUtilities.lockOrientation(parentAlert.baseFragment.getParentActivity(), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        saveLastCameraBitmap();
        int count = gridView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = gridView.getChildAt(a);
            if (child instanceof PhotoAttachCameraCell) {
                child.setVisibility(View.VISIBLE);
                ((PhotoAttachCameraCell) child).updateBitmap();
                break;
            }
        }
        cameraView.destroy(async, null);
        if (cameraInitAnimation != null) {
            cameraInitAnimation.cancel();
            cameraInitAnimation = null;
        }
        AndroidUtilities.runOnUIThread(() -> {
            parentAlert.getContainer().removeView(windowView);
            parentAlert.getContainer().removeView(cameraIcon);
            cameraView = null;
            cameraIcon = null;
        }, 300);
        canSaveCameraPreview = false;
    }

    private void saveLastCameraBitmap() {
        if (!canSaveCameraPreview) {
            return;
        }
        try {
            TextureView textureView = cameraView.getTextureView();
            Bitmap bitmap = textureView.getBitmap();
            if (bitmap != null) {
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), cameraView.getMatrix(), true);
                bitmap.recycle();
                bitmap = newBitmap;
                Bitmap lastBitmap = Bitmap.createScaledBitmap(bitmap, 80, (int) (bitmap.getHeight() / (bitmap.getWidth() / 80.0f)), true);
                if (lastBitmap != null) {
                    if (lastBitmap != bitmap) {
                        bitmap.recycle();
                    }
                    Utilities.blurBitmap(lastBitmap, 7, 1, lastBitmap.getWidth(), lastBitmap.getHeight(), lastBitmap.getRowBytes());
                    File file = new File(ApplicationLoader.getFilesDirFixed(), "cthumb.jpg");
                    FileOutputStream stream = new FileOutputStream(file);
                    lastBitmap.compress(Bitmap.CompressFormat.JPEG, 87, stream);
                    lastBitmap.recycle();
                    stream.close();
                }
            }
        } catch (Throwable ignore) {

        }
    }

    private void saveLastCameraBitmap(Runnable whenDone) {
        if (cameraView == null || cameraView.getTextureView() == null) {
            return;
        }
        try {
            TextureView textureView = cameraView.getTextureView();
            final Bitmap bitmap = textureView.getBitmap();
            Utilities.themeQueue.postRunnable(() -> {
                try {
                    if (bitmap != null) {
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), cameraView.getMatrix(), true);
                        bitmap.recycle();
                        Bitmap bitmap2 = newBitmap;
                        Bitmap lastBitmap = Bitmap.createScaledBitmap(bitmap2, 80, (int) (bitmap2.getHeight() / (bitmap2.getWidth() / 80.0f)), true);
                        if (lastBitmap != null) {
                            if (lastBitmap != bitmap2) {
                                bitmap2.recycle();
                            }
                            Utilities.blurBitmap(lastBitmap, 7, 1, lastBitmap.getWidth(), lastBitmap.getHeight(), lastBitmap.getRowBytes());
                            File file = new File(ApplicationLoader.getFilesDirFixed(), "cthumb.jpg");
                            FileOutputStream stream = new FileOutputStream(file);
                            lastBitmap.compress(Bitmap.CompressFormat.JPEG, 87, stream);
                            lastBitmap.recycle();
                            stream.close();
                        }
                    }
                } catch (Throwable ignore) {

                } finally {
                    AndroidUtilities.runOnUIThread(whenDone);
                }
            });
        } catch (Throwable ignore) {}
    }

    public void onActivityResultFragment(int requestCode, Intent data, String currentPicturePath) {
        if (parentAlert.destroyed) {
            return;
        }
        mediaFromExternalCamera = true;
        if (requestCode == 0) {
            PhotoViewer.getInstance().setParentActivity(parentAlert.baseFragment.getParentActivity(), resourcesProvider);
            PhotoViewer.getInstance().setMaxSelectedPhotos(parentAlert.maxSelectedPhotos, parentAlert.allowOrder);
            Pair<Integer, Integer> orientation = AndroidUtilities.getImageOrientation(currentPicturePath);
            int width = 0, height = 0;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(new File(currentPicturePath).getAbsolutePath(), options);
                width = options.outWidth;
                height = options.outHeight;
            } catch (Exception ignore) {}
            MediaController.PhotoEntry photoEntry = new MediaController.PhotoEntry(0, lastImageId--, 0, currentPicturePath, orientation.first, false, width, height, 0).setOrientation(orientation);
            photoEntry.canDeleteAfter = true;
            openPhotoViewer(photoEntry, false, true);
        } else if (requestCode == 2) {
            String videoPath = null;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("pic path " + currentPicturePath);
            }
            if (data != null && currentPicturePath != null) {
                if (new File(currentPicturePath).exists()) {
                    data = null;
                }
            }
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("video record uri " + uri.toString());
                    }
                    videoPath = AndroidUtilities.getPath(uri);
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("resolved path = " + videoPath);
                    }
                    if (videoPath == null || !(new File(videoPath).exists())) {
                        videoPath = currentPicturePath;
                    }
                } else {
                    videoPath = currentPicturePath;
                }
                if (!(parentAlert.baseFragment instanceof ChatActivity) || !((ChatActivity) parentAlert.baseFragment).isSecretChat()) {
                    AndroidUtilities.addMediaToGallery(currentPicturePath);
                }
                currentPicturePath = null;
            }
            if (videoPath == null && currentPicturePath != null) {
                File f = new File(currentPicturePath);
                if (f.exists()) {
                    videoPath = currentPicturePath;
                }
            }

            MediaMetadataRetriever mediaMetadataRetriever = null;
            long duration = 0;
            try {
                mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(videoPath);
                String d = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (d != null) {
                    duration = (int) Math.ceil(Long.parseLong(d) / 1000.0f);
                }
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                try {
                    if (mediaMetadataRetriever != null) {
                        mediaMetadataRetriever.release();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            final Bitmap bitmap = SendMessagesHelper.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            String fileName = Integer.MIN_VALUE + "_" + SharedConfig.getLastLocalId() + ".jpg";
            final File cacheFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName);
            try {
                FileOutputStream stream = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 55, stream);
            } catch (Throwable e) {
                FileLog.e(e);
            }
            SharedConfig.saveConfig();

            MediaController.PhotoEntry entry = new MediaController.PhotoEntry(0, lastImageId--, 0, videoPath, 0, true, bitmap.getWidth(), bitmap.getHeight(), 0);
            entry.duration = (int) duration;
            entry.thumbPath = cacheFile.getAbsolutePath();
            openPhotoViewer(entry, false, true);
        }
    }

    float additionCloseCameraY;

    public void closeCamera(boolean animated) {
        cameraView.fromChatAttachAlertPhotoLayout = true;
        if (takingPhoto || cameraView == null) {
            return;
        }
        animateCameraValues[1] = itemSize;
        animateCameraValues[2] = itemSize;
        if (zoomControlHideRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(zoomControlHideRunnable);
            zoomControlHideRunnable = null;
        }
        AndroidUtilities.setLightNavigationBar(parentAlert.getWindow(), AndroidUtilities.computePerceivedBrightness(getThemedColor(Theme.key_windowBackgroundGray)) > 0.721);
        if (animated) {
            additionCloseCameraY = cameraView.getTranslationY();

            cameraAnimationInProgress = true;
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(ObjectAnimator.ofFloat(this, "cameraOpenProgress", 0.0f));
            animators.add(ObjectAnimator.ofFloat(zoomControlView, View.ALPHA, 0.0f));
            animators.add(ObjectAnimator.ofFloat(counterTextView, View.ALPHA, 0.0f));
            animators.add(ObjectAnimator.ofFloat(cameraPhotoRecyclerView, View.ALPHA, 0.0f));

            notificationsLocker.lock();
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.setDuration(220);
            animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    notificationsLocker.unlock();
                    cameraExpanded = false;
                    parentAlert.getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
                    setCameraOpenProgress(0f);
                    cameraAnimationInProgress = false;
                    if (cameraView != null) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            cameraView.invalidateOutline();
                        } else {
                            cameraView.invalidate();
                        }
                    }
                    cameraOpened = false;

                    if (cameraPanel != null) {
                        cameraPanel.setVisibility(View.GONE);
                    }
                    if (zoomControlView != null) {
                        zoomControlView.setVisibility(View.GONE);
                        zoomControlView.setTag(null);
                    }
                    if (cameraPhotoRecyclerView != null) {
                        cameraPhotoRecyclerView.setVisibility(View.GONE);
                    }
                    if (cameraView != null) {
                        cameraView.setFpsLimit(30);
                        if (Build.VERSION.SDK_INT >= 21) {
                            cameraView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                        }
                    }
                }
            });
            animatorSet.start();
        } else {
            cameraExpanded = false;
            parentAlert.getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
            setCameraOpenProgress(0f);
            animateCameraValues[0] = 0;
            setCameraOpenProgress(0);
            cameraPanel.setAlpha(0);
            cameraPanel.setVisibility(View.GONE);
            zoomControlView.setAlpha(0);
            zoomControlView.setTag(null);
            zoomControlView.setVisibility(View.GONE);
            cameraPhotoRecyclerView.setAlpha(0);
            counterTextView.setAlpha(0);
            cameraPhotoRecyclerView.setVisibility(View.GONE);
            cameraOpened = false;
            if (cameraView != null) {
                cameraView.setFpsLimit(30);
                if (Build.VERSION.SDK_INT >= 21) {
                    cameraView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            }
        }
        if (windowView != null) {
            windowView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            gridView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }

        if (!LiteMode.isEnabled(LiteMode.FLAGS_CHAT) && cameraView != null) {
            cameraView.showTexture(false, animated);
        }
    }

    float animationClipTop;
    float animationClipBottom;
    float animationClipRight;
    float animationClipLeft;

    @Keep
    public void setCameraOpenProgress(float value) {
        if (windowView == null) {
            return;
        }
        cameraOpenProgress = value;

        // Dimensions for the starting and ending animation states
        float startWidth = animateCameraValues[1];
        float startHeight = animateCameraValues[2];
        float endWidth = parentAlert.getContainer().getWidth() - parentAlert.getLeftInset() - parentAlert.getRightInset();
        float endHeight = parentAlert.getContainer().getHeight();

        // Translation values for the animation
        float fromX = cameraViewLocation[0];
        float fromY = cameraViewLocation[1];
        float toX = 0;
        float toY = additionCloseCameraY;

        // Alpha animation for the camera icon
        if (value == 0) {
            cameraIcon.setTranslationX(cameraViewLocation[0]);
            cameraIcon.setTranslationY(cameraViewLocation[1] + cameraViewOffsetY);
        }

        // Variables for windowView's width and height
        int windowViewW, windowViewH;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) windowView.getLayoutParams();

        // Texture height for scaling
        float textureStartHeight = cameraView.getTextureHeight(startWidth, startHeight);
        float textureEndHeight = cameraView.getTextureHeight(endWidth, endHeight);

        float fromScale = textureStartHeight / textureEndHeight;
        float fromScaleY = startHeight / endHeight;
        float fromScaleX = startWidth / endWidth;

        if (cameraExpanded) {
            // Expand to full screen
            windowViewW = (int) endWidth;
            windowViewH = (int) endHeight;

            // Scale windowView
            final float s = fromScale * (1f - value) + value;
            windowView.setScaleX(s);
            windowView.setScaleY(s);

            // Scale windowView's X and Y independently
            final float sX = fromScaleX * (1f - value) + value;
            final float sY = fromScaleY * (1f - value) + value;

            // Calculate scale offsets
            final float scaleOffsetY = (1 - sY) * endHeight / 2;
            final float scaleOffsetX = (1 - sX) * endWidth / 2;

            // Translate windowView
            windowView.setTranslationX(fromX * (1f - value) + toX * value - scaleOffsetX);
            windowView.setTranslationY(fromY * (1f - value) + toY * value - scaleOffsetY);

            // Update clipping values
            animationClipTop = fromY * (1f - value) - windowView.getTranslationY();
            animationClipBottom = ((fromY + startHeight) * (1f - value) - windowView.getTranslationY()) + endHeight * value;
            animationClipLeft = fromX * (1f - value) - windowView.getTranslationX();
            animationClipRight = ((fromX + startWidth) * (1f - value) - windowView.getTranslationX()) + endWidth * value;
        } else {
            // Collapse back to the original size
            windowViewW = (int) startWidth;
            windowViewH = (int) startHeight;

            // Reset scale
            windowView.setScaleX(1f);
            windowView.setScaleY(1f);

            // Reset clipping
            animationClipTop = 0;
            animationClipBottom = endHeight;
            animationClipLeft = 0;
            animationClipRight = endWidth;

            // Reset translation
            windowView.setTranslationX(fromX);
            windowView.setTranslationY(fromY);
        }

        // Update camera icon alpha
        if (value <= 0.5f) {
            cameraIcon.setAlpha(1.0f - value / 0.5f);
        } else {
            cameraIcon.setAlpha(0.0f);
        }

        // Update windowView layout parameters
        if (layoutParams.width != windowViewW || layoutParams.height != windowViewH) {
            layoutParams.width = windowViewW;
            layoutParams.height = windowViewH;
            windowView.requestLayout();
        }

        // Invalidate windowView's outline for proper rendering
        if (Build.VERSION.SDK_INT >= 21) {
            windowView.invalidateOutline();
        } else {
            windowView.invalidate();
        }
    }


    @Keep
    public float getCameraOpenProgress() {
        return cameraOpenProgress;
    }

    protected void checkCameraViewPosition() {
        if (PhotoViewer.hasInstance() && PhotoViewer.getInstance().stickerMakerView != null && PhotoViewer.getInstance().stickerMakerView.isThanosInProgress) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (windowView != null) {
                windowView.invalidateOutline();
            }
            RecyclerView.ViewHolder holder = gridView.findViewHolderForAdapterPosition(itemsPerRow - 1);
            if (holder != null) {
                holder.itemView.invalidateOutline();
            }
            if (!adapter.needCamera || !deviceHasGoodCamera || selectedAlbumEntry != galleryAlbumEntry) {
                holder = gridView.findViewHolderForAdapterPosition(0);
                if (holder != null) {
                    holder.itemView.invalidateOutline();
                }
            }
        }
        if (windowView != null) {
            windowView.invalidate();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && recordTime != null) {
            MarginLayoutParams params = (MarginLayoutParams) recordTime.getLayoutParams();
            params.topMargin = (getRootWindowInsets() == null ? dp(16)  : getRootWindowInsets().getSystemWindowInsetTop() + dp(2));
        }

        if (!deviceHasGoodCamera) {
            return;
        }
        int count = gridView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = gridView.getChildAt(a);
            if (child instanceof PhotoAttachCameraCell) {
                if (Build.VERSION.SDK_INT >= 19) {
                    if (!child.isAttachedToWindow()) {
                        break;
                    }
                }

                float topLocal = child.getY() + gridView.getY() + getY();
                float top = topLocal + parentAlert.getSheetContainer().getY();
                float left = child.getX() + gridView.getX() + getX() + parentAlert.getSheetContainer().getX();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    left -= getRootWindowInsets().getSystemWindowInsetLeft();
                }

                float maxY = (Build.VERSION.SDK_INT >= 21 && !parentAlert.inBubbleMode ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + parentAlert.topCommentContainer.getMeasuredHeight() * parentAlert.topCommentContainer.getAlpha();
                if (parentAlert.mentionContainer != null && parentAlert.mentionContainer.isReversed()) {
                    maxY = Math.max(maxY, parentAlert.mentionContainer.getY() + parentAlert.mentionContainer.clipTop() - parentAlert.currentPanTranslationY);
                }
                float newCameraViewOffsetY;
                if (topLocal < maxY) {
                    newCameraViewOffsetY = maxY - topLocal;
                } else {
                    newCameraViewOffsetY = 0;
                }

                if (newCameraViewOffsetY != cameraViewOffsetY) {
                    cameraViewOffsetY = newCameraViewOffsetY;
                    if (cameraView != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            windowView.invalidateOutline();
                        } else {
                            windowView.invalidate();
                        }
                    }
                    if (cameraIcon != null) {
                        cameraIcon.invalidate();
                    }
                }

                int containerHeight = parentAlert.getSheetContainer().getMeasuredHeight();
                maxY = (int) (containerHeight - parentAlert.buttonsRecyclerView.getMeasuredHeight() + parentAlert.buttonsRecyclerView.getTranslationY());
                if (parentAlert.mentionContainer != null) {
                    maxY -= parentAlert.mentionContainer.clipBottom() - dp(6);
                }

                if (topLocal + child.getMeasuredHeight() > maxY) {
                    cameraViewOffsetBottomY = Math.min(-dp(5), topLocal - maxY) + child.getMeasuredHeight();
                } else {
                    cameraViewOffsetBottomY = 0;
                }

                cameraViewLocation[0] = left;
                cameraViewLocation[1] = top;
                applyCameraViewPosition();
                return;
            }
        }


        if (cameraViewOffsetY != 0 || cameraViewOffsetX != 0) {
            cameraViewOffsetX = 0;
            cameraViewOffsetY = 0;
            if (windowView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    windowView.invalidateOutline();
                } else {
                    windowView.invalidate();
                }
            }
            if (cameraIcon != null) {
                cameraIcon.invalidate();
            }
        }

        cameraViewLocation[0] = dp(-400);
        cameraViewLocation[1] = 0;

        applyCameraViewPosition();
    }

    private void applyCameraViewPosition() {
        if (cameraView != null) {
            if (!cameraOpened) {
                windowView.setTranslationX(cameraViewLocation[0]);
                windowView.setTranslationY(cameraViewLocation[1] + currentPanTranslationY);
            }
            cameraIcon.setTranslationX(cameraViewLocation[0]);
            cameraIcon.setTranslationY(cameraViewLocation[1] + cameraViewOffsetY + currentPanTranslationY);
            int finalWidth = itemSize;
            int finalHeight = itemSize;

            LayoutParams layoutParams;
            if (!cameraOpened) {
                layoutParams = (LayoutParams) windowView.getLayoutParams();
                if (layoutParams.height != finalHeight || layoutParams.width != finalWidth) {
                    layoutParams.width = finalWidth;
                    layoutParams.height = finalHeight;
                    windowView.setLayoutParams(layoutParams);
                    final LayoutParams layoutParamsFinal = layoutParams;
                    AndroidUtilities.runOnUIThread(() -> {
                        if (windowView != null) {
                            windowView.setLayoutParams(layoutParamsFinal);
                        }
                    });
                }
            }

            finalWidth = (int) (itemSize - cameraViewOffsetX);
            finalHeight = (int) (itemSize - cameraViewOffsetY - cameraViewOffsetBottomY);

            layoutParams = (LayoutParams) cameraIcon.getLayoutParams();
            if (layoutParams.height != finalHeight || layoutParams.width != finalWidth) {
                layoutParams.width = finalWidth;
                layoutParams.height = finalHeight;
                cameraIcon.setLayoutParams(layoutParams);
                final LayoutParams layoutParamsFinal = layoutParams;
                AndroidUtilities.runOnUIThread(() -> {
                    if (cameraIcon != null) {
                        cameraIcon.setLayoutParams(layoutParamsFinal);
                    }
                });
            }
        }
    }

    public HashMap<Object, Object> getSelectedPhotos() {
        return selectedPhotos;
    }

    public ArrayList<Object> getSelectedPhotosOrder() {
        return selectedPhotosOrder;
    }

    public void updateSelected(HashMap<Object, Object> newSelectedPhotos, ArrayList<Object> newPhotosOrder, boolean updateLayout) {
        selectedPhotos.clear();
        selectedPhotos.putAll(newSelectedPhotos);
        selectedPhotosOrder.clear();
        selectedPhotosOrder.addAll(newPhotosOrder);
        if (updateLayout) {
            updatePhotosCounter(false);
            updateCheckedPhotoIndices();

            final int count = gridView.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = gridView.getChildAt(i);
                if (child instanceof PhotoAttachPhotoCell) {
                    int position = gridView.getChildAdapterPosition(child);
                    if (adapter.needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                        position--;
                    }

                    PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) child;
                    if (parentAlert.avatarPicker != 0) {
                        cell.getCheckBox().setVisibility(GONE);
                    }
                    MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(position);
                    if (photoEntry != null) {
                        cell.setPhotoEntry(photoEntry, selectedPhotos.size() > 1, adapter.needCamera && selectedAlbumEntry == galleryAlbumEntry, position == adapter.getItemCount() - 1);
                        if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                            cell.setChecked(selectedPhotosOrder.indexOf(photoEntry.imageId), selectedPhotos.containsKey(photoEntry.imageId), false);
                        } else {
                            cell.setChecked(-1, selectedPhotos.containsKey(photoEntry.imageId), false);
                        }
                    }
                }
            }
        }
    }

    private boolean isNoGalleryPermissions() {
        Activity activity = AndroidUtilities.findActivity(getContext());
        if (activity == null) {
            activity = parentAlert.baseFragment.getParentActivity();
        }
        return Build.VERSION.SDK_INT >= 23 && (
            activity == null ||
            Build.VERSION.SDK_INT >= 33 && (
                    activity.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                            activity.checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
            ) ||
            Build.VERSION.SDK_INT < 33 && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        );
    }

    public void checkStorage() {
        if (noGalleryPermissions && Build.VERSION.SDK_INT >= 23) {
            final Activity activity = parentAlert.baseFragment.getParentActivity();

            noGalleryPermissions = isNoGalleryPermissions();
            if (!noGalleryPermissions) {
                loadGalleryPhotos();
            }
            adapter.notifyDataSetChanged();
            cameraAttachAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void scrollToTop() {
        gridView.smoothScrollToPosition(0);
    }

    @Override
    public int needsActionBar() {
        return 1;
    }

    @Override
    public void onMenuItemClick(int id) {
        if (id == caption) {
            parentAlert.setCaptionAbove(!parentAlert.captionAbove);
            captionItem.setState(!parentAlert.captionAbove, true);
            return;
        }
        if (id == group || id == compress) {
            if (parentAlert.maxSelectedPhotos > 0 && selectedPhotosOrder.size() > 1) {
                TLRPC.Chat chat = parentAlert.getChat();
                if (chat != null && !ChatObject.hasAdminRights(chat) && chat.slowmode_enabled) {
                    AlertsCreator.createSimpleAlert(getContext(), LocaleController.getString(R.string.Slowmode), LocaleController.getString(R.string.SlowmodeSendError), resourcesProvider).show();
                    return;
                }
            }
        }
        if (id == group) {
            if (parentAlert.editingMessageObject == null && parentAlert.baseFragment instanceof ChatActivity && ((ChatActivity) parentAlert.baseFragment).isInScheduleMode()) {
                AlertsCreator.createScheduleDatePickerDialog(getContext(), ((ChatActivity) parentAlert.baseFragment).getDialogId(), (notify, scheduleDate) -> {
                    parentAlert.applyCaption();
                    parentAlert.delegate.didPressedButton(7, false, notify, scheduleDate, 0, parentAlert.isCaptionAbove(), false);
                }, resourcesProvider);
            } else {
                parentAlert.applyCaption();
                parentAlert.delegate.didPressedButton(7, false, true, 0, 0, parentAlert.isCaptionAbove(), false);
            }
        } else if (id == compress) {
            if (parentAlert.editingMessageObject == null && parentAlert.baseFragment instanceof ChatActivity && ((ChatActivity) parentAlert.baseFragment).isInScheduleMode()) {
                AlertsCreator.createScheduleDatePickerDialog(getContext(), ((ChatActivity) parentAlert.baseFragment).getDialogId(), (notify, scheduleDate) -> {
                    parentAlert.applyCaption();
                    parentAlert.delegate.didPressedButton(4, true, notify, scheduleDate, 0, parentAlert.isCaptionAbove(), false);
                }, resourcesProvider);
            } else {
                parentAlert.applyCaption();
                parentAlert.delegate.didPressedButton(4, true, true, 0, 0, parentAlert.isCaptionAbove(), false);
            }
        } else if (id == spoiler) {
            if (parentAlert.getPhotoPreviewLayout() != null) {
                parentAlert.getPhotoPreviewLayout().startMediaCrossfade();
            }

            boolean spoilersEnabled = false;
            for (Map.Entry<Object, Object> en : selectedPhotos.entrySet()) {
                MediaController.PhotoEntry entry = (MediaController.PhotoEntry) en.getValue();
                if (entry.hasSpoiler) {
                    spoilersEnabled = true;
                    break;
                }
            }
            spoilersEnabled = !spoilersEnabled;
            boolean finalSpoilersEnabled = spoilersEnabled;
            AndroidUtilities.runOnUIThread(()-> {
                spoilerItem.setText(LocaleController.getString(finalSpoilersEnabled ? R.string.DisablePhotoSpoiler : R.string.EnablePhotoSpoiler));
                if (finalSpoilersEnabled) {
                    spoilerItem.setIcon(R.drawable.msg_spoiler_off);
                } else {
                    spoilerItem.setAnimatedIcon(R.raw.photo_spoiler);
                }
                if (finalSpoilersEnabled) {
                    parentAlert.selectedMenuItem.hideSubItem(compress);
                    if (getSelectedItemsCount() <= 1) {
                        parentAlert.selectedMenuItem.hideSubItem(media_gap);
                    }
                } else {
                    parentAlert.selectedMenuItem.showSubItem(compress);
                    if (getSelectedItemsCount() <= 1) {
                        parentAlert.selectedMenuItem.showSubItem(media_gap);
                    }
                }
            }, 200);

            List<Integer> selectedIds = new ArrayList<>();
            for (HashMap.Entry<Object, Object> entry : selectedPhotos.entrySet()) {
                if (entry.getValue() instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) entry.getValue();
                    photoEntry.hasSpoiler = spoilersEnabled;
                    photoEntry.isChatPreviewSpoilerRevealed = false;
                    photoEntry.isAttachSpoilerRevealed = false;
                    selectedIds.add(photoEntry.imageId);
                }
            }

            gridView.forAllChild(view -> {
                if (view instanceof PhotoAttachPhotoCell) {
                    MediaController.PhotoEntry entry = ((PhotoAttachPhotoCell) view).getPhotoEntry();
                    ((PhotoAttachPhotoCell) view).setHasSpoiler(entry != null && selectedIds.contains(entry.imageId) && finalSpoilersEnabled);
                }
            });
            if (parentAlert.getCurrentAttachLayout() != this) {
                adapter.notifyDataSetChanged();
            }

            if (parentAlert.getPhotoPreviewLayout() != null) {
                parentAlert.getPhotoPreviewLayout().invalidateGroupsView();
            }
        } else if (id == open_in) {
            try {
                if (parentAlert.baseFragment instanceof ChatActivity || parentAlert.avatarPicker == 2) {
                    Intent videoPickerIntent = new Intent();
                    videoPickerIntent.setType("video/*");
                    videoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                    videoPickerIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, FileLoader.DEFAULT_MAX_FILE_SIZE);

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{videoPickerIntent});

                    if (parentAlert.avatarPicker != 0) {
                        parentAlert.baseFragment.startActivityForResult(chooserIntent, 14);
                    } else {
                        parentAlert.baseFragment.startActivityForResult(chooserIntent, 1);
                    }
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    if (parentAlert.avatarPicker != 0) {
                        parentAlert.baseFragment.startActivityForResult(photoPickerIntent, 14);
                    } else {
                        parentAlert.baseFragment.startActivityForResult(photoPickerIntent, 1);
                    }
                }
                parentAlert.dismiss(true);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (id == preview) {
            parentAlert.updatePhotoPreview(parentAlert.getCurrentAttachLayout() != parentAlert.getPhotoPreviewLayout());
        } else if (id == stars) {
            StarsIntroActivity.showMediaPriceSheet(getContext(), getStarsPrice(), true, (price, done) -> {
                done.run();
                setStarsPrice(price);
            }, resourcesProvider);
        } else if (id >= 10) {
            selectedAlbumEntry = dropDownAlbums.get(id - 10);
            if (selectedAlbumEntry == galleryAlbumEntry) {
                dropDown.setText(LocaleController.getString(R.string.ChatGallery));
            } else {
                dropDown.setText(selectedAlbumEntry.bucketName);
            }
            adapter.notifyDataSetChanged();
            cameraAttachAdapter.notifyDataSetChanged();
            layoutManager.scrollToPositionWithOffset(0, -gridView.getPaddingTop() + dp(7));
        }
    }

    @Override
    public int getSelectedItemsCount() {
        return selectedPhotosOrder.size();
    }

    @Override
    public void onSelectedItemsCountChanged(int count) {
        final boolean hasCompress;
        final boolean hasGroup;
        if (count <= 1 || parentAlert.editingMessageObject != null) {
            hasGroup = false;
            parentAlert.selectedMenuItem.hideSubItem(group);
            if (count == 0) {
                parentAlert.selectedMenuItem.showSubItem(open_in);
                hasCompress = false;
                parentAlert.selectedMenuItem.hideSubItem(compress);
            } else if (documentsEnabled && getStarsPrice() <= 0 && parentAlert.editingMessageObject == null) {
                hasCompress = true;
                parentAlert.selectedMenuItem.showSubItem(compress);
            } else {
                hasCompress = false;
                parentAlert.selectedMenuItem.hideSubItem(compress);
            }
        } else {
            if (getStarsPrice() <= 0) {
                hasGroup = true;
                parentAlert.selectedMenuItem.showSubItem(group);
            } else {
                hasGroup = false;
                parentAlert.selectedMenuItem.hideSubItem(group);
            }
            if (documentsEnabled && getStarsPrice() <= 0) {
                hasCompress = true;
                parentAlert.selectedMenuItem.showSubItem(compress);
            } else {
                hasCompress = false;
                parentAlert.selectedMenuItem.hideSubItem(compress);
            }
        }
        if (count != 0) {
            parentAlert.selectedMenuItem.hideSubItem(open_in);
        }
        if (count > 1) {
            parentAlert.selectedMenuItem.showSubItem(preview_gap);
            parentAlert.selectedMenuItem.showSubItem(preview);
            compressItem.setText(LocaleController.getString(R.string.SendAsFiles));
        } else {
            parentAlert.selectedMenuItem.hideSubItem(preview_gap);
            parentAlert.selectedMenuItem.hideSubItem(preview);
            if (count != 0) {
                compressItem.setText(LocaleController.getString(R.string.SendAsFile));
            }
        }
        final boolean hasSpoiler = count > 0 && getStarsPrice() <= 0 && (parentAlert == null || parentAlert.baseFragment instanceof ChatActivity && !((ChatActivity) parentAlert.baseFragment).isSecretChat());
        final boolean hasCaption = count > 0 && parentAlert != null && parentAlert.hasCaption() && parentAlert.baseFragment instanceof ChatActivity;
        final boolean hasStars = count > 0 && (parentAlert != null && parentAlert.baseFragment instanceof ChatActivity && ChatObject.isChannelAndNotMegaGroup(((ChatActivity) parentAlert.baseFragment).getCurrentChat()) && ((ChatActivity) parentAlert.baseFragment).getCurrentChatInfo() != null && ((ChatActivity) parentAlert.baseFragment).getCurrentChatInfo().paid_media_allowed);
        if (!hasSpoiler) {
            spoilerItem.setText(LocaleController.getString(R.string.EnablePhotoSpoiler));
            spoilerItem.setAnimatedIcon(R.raw.photo_spoiler);
            parentAlert.selectedMenuItem.hideSubItem(spoiler);
        } else if (parentAlert != null) {
            parentAlert.selectedMenuItem.showSubItem(spoiler);
        }
        if (hasCaption) {
            captionItem.setVisibility(View.VISIBLE);
        } else {
            captionItem.setVisibility(View.GONE);
        }
        if ((hasSpoiler || hasCaption) && (hasCompress || hasGroup)) {
            parentAlert.selectedMenuItem.showSubItem(media_gap);
        } else {
            parentAlert.selectedMenuItem.hideSubItem(media_gap);
        }
        if (hasStars) {
            updateStarsItem();
            updatePhotoStarsPrice();
            parentAlert.selectedMenuItem.showSubItem(stars);
        } else {
            parentAlert.selectedMenuItem.hideSubItem(stars);
        }
    }

    private void updateStarsItem() {
        if (starsItem == null) return;
        long amount = getStarsPrice();
        if (amount > 0) {
            starsItem.setText(getString(R.string.PaidMediaPriceButton));
            starsItem.setSubtext(formatPluralString("Stars", (int) amount));
        } else {
            starsItem.setText(getString(R.string.PaidMediaButton));
            starsItem.setSubtext(null);
        }
    }

    @Override
    public void applyCaption(CharSequence text) {
        for (int a = 0; a < selectedPhotosOrder.size(); a++) {
            if (a == 0) {
                final Object key = selectedPhotosOrder.get(a);
                Object o = selectedPhotos.get(key);
                if (o instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry1 = (MediaController.PhotoEntry) o;
                    photoEntry1 = photoEntry1.clone();
                    CharSequence[] caption = new CharSequence[] { text };
                    photoEntry1.entities = MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(caption, false);
                    photoEntry1.caption = caption[0];
                    o = photoEntry1;
                } else if (o instanceof MediaController.SearchImage) {
                    MediaController.SearchImage photoEntry1 = (MediaController.SearchImage) o;
                    photoEntry1 = photoEntry1.clone();
                    CharSequence[] caption = new CharSequence[] { text };
                    photoEntry1.entities = MediaDataController.getInstance(UserConfig.selectedAccount).getEntities(caption, false);
                    photoEntry1.caption = caption[0];
                    o = photoEntry1;
                }
                selectedPhotos.put(key, o);
            }
        }
    }

    public boolean captionForAllMedia() {
        int captionCount = 0;
        for (int a = 0; a < selectedPhotosOrder.size(); a++) {
            Object o = selectedPhotos.get(selectedPhotosOrder.get(a));
            CharSequence caption = null;
            if (o instanceof MediaController.PhotoEntry) {
                MediaController.PhotoEntry photoEntry1 = (MediaController.PhotoEntry) o;
                caption = photoEntry1.caption;
            } else if (o instanceof MediaController.SearchImage) {
                MediaController.SearchImage photoEntry1 = (MediaController.SearchImage) o;
                caption = photoEntry1.caption;
            }
            if (!TextUtils.isEmpty(caption)) {
                captionCount++;
            }
        }
        return captionCount <= 1;
    }

    @Override
    public void onDestroy() {
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.cameraInitied);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.albumsDidLoad);
    }

    @Override
    public void onPause() {
        if (shutterButton == null) {
            return;
        }
        if (!requestingPermissions) {
            if (cameraView != null && shutterButton.getState() == ShutterButton.State.RECORDING) {
                resetRecordState();
                CameraController.getInstance().stopVideoRecording(cameraView.getCameraSession(), false);
                shutterButton.setState(ShutterButton.State.DEFAULT, true);
            }
            if (cameraOpened) {
                closeCamera(false);
            }
            hideCamera(true);
        } else {
            if (cameraView != null && shutterButton.getState() == ShutterButton.State.RECORDING) {
                shutterButton.setState(ShutterButton.State.DEFAULT, true);
            }
            requestingPermissions = false;
        }
    }

    @Override
    public void onResume() {
        if (parentAlert.isShowing() && !parentAlert.isDismissed() && !PhotoViewer.getInstance().isVisible()) {
            checkCamera(false);
        }
    }

    @Override
    public int getListTopPadding() {
        return gridView.getPaddingTop();
    }

    public int currentItemTop = 0;

    @Override
    public int getCurrentItemTop() {
        if (gridView.getChildCount() <= 0) {
            gridView.setTopGlowOffset(currentItemTop = gridView.getPaddingTop());
            progressView.setTranslationY(0);
            return Integer.MAX_VALUE;
        }
        View child = gridView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) gridView.findContainingViewHolder(child);
        int top = child.getTop();
        int newOffset = dp(7);
        if (top >= dp(7) && holder != null && holder.getAdapterPosition() == 0) {
            newOffset = top;
        }
        progressView.setTranslationY(newOffset + (getMeasuredHeight() - newOffset - dp(50) - progressView.getMeasuredHeight()) / 2);
        gridView.setTopGlowOffset(newOffset);
        return currentItemTop = newOffset;
    }

    @Override
    public int getFirstOffset() {
        return getListTopPadding() + dp(56);
    }

    @Override
    public void checkColors() {
        if (cameraIcon != null) {
            cameraIcon.invalidate();
        }
        int textColor = forceDarkTheme ? Theme.key_voipgroup_actionBarItems : Theme.key_dialogTextBlack;
        Theme.setDrawableColor(cameraDrawable, getThemedColor(Theme.key_dialogCameraIcon));
        progressView.setTextColor(getThemedColor(Theme.key_emptyListPlaceholder));
        gridView.setGlowColor(getThemedColor(Theme.key_dialogScrollGlow));
        RecyclerView.ViewHolder holder = gridView.findViewHolderForAdapterPosition(0);
        if (holder != null && holder.itemView instanceof PhotoAttachCameraCell) {
            ((PhotoAttachCameraCell) holder.itemView).getImageView().setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogCameraIcon), PorterDuff.Mode.MULTIPLY));
        }

        dropDown.setTextColor(getThemedColor(textColor));
        dropDownContainer.setPopupItemsColor(getThemedColor(forceDarkTheme ? Theme.key_voipgroup_actionBarItems : Theme.key_actionBarDefaultSubmenuItem), false);
        dropDownContainer.setPopupItemsColor(getThemedColor(forceDarkTheme ? Theme.key_voipgroup_actionBarItems :Theme.key_actionBarDefaultSubmenuItem), true);
        dropDownContainer.redrawPopup(getThemedColor(forceDarkTheme ? Theme.key_voipgroup_actionBarUnscrolled : Theme.key_actionBarDefaultSubmenuBackground));
        Theme.setDrawableColor(dropDownDrawable, getThemedColor(textColor));
    }

    @Override
    public void onInit(boolean hasVideo, boolean hasPhoto, boolean hasDocuments) {
        mediaEnabled = hasVideo || hasPhoto;
        videoEnabled = hasVideo;
        photoEnabled = hasPhoto;
        documentsEnabled = hasDocuments;
        if (cameraView != null) {
            cameraView.setAlpha(mediaEnabled ? 1.0f : 0.2f);
            cameraView.setEnabled(mediaEnabled);
        }
        if (cameraIcon != null) {
            cameraIcon.setAlpha(mediaEnabled ? 1.0f : 0.2f);
            cameraIcon.setEnabled(mediaEnabled);
        }
        if ((parentAlert.baseFragment instanceof ChatActivity || parentAlert.getChat() != null) && parentAlert.avatarPicker == 0) {
            galleryAlbumEntry = MediaController.allMediaAlbumEntry;
            if (mediaEnabled) {
                progressView.setText(LocaleController.getString(R.string.NoPhotos));
                progressView.setLottie(0, 0, 0);
            } else {
                TLRPC.Chat chat = parentAlert.getChat();
                progressView.setLottie(R.raw.media_forbidden, 150, 150);
                if (ChatObject.isActionBannedByDefault(chat, ChatObject.ACTION_SEND_MEDIA)) {
                    progressView.setText(LocaleController.getString(R.string.GlobalAttachMediaRestricted));
                } else if (AndroidUtilities.isBannedForever(chat.banned_rights)) {
                    progressView.setText(LocaleController.formatString("AttachMediaRestrictedForever", R.string.AttachMediaRestrictedForever));
                } else {
                    progressView.setText(LocaleController.formatString("AttachMediaRestricted", R.string.AttachMediaRestricted, LocaleController.formatDateForBan(chat.banned_rights.until_date)));
                }
            }
        } else {
            if (shouldLoadAllMedia()) {
                galleryAlbumEntry = MediaController.allMediaAlbumEntry;
            } else {
                galleryAlbumEntry = MediaController.allPhotosAlbumEntry;
            }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            noGalleryPermissions = isNoGalleryPermissions();
        }
        if (galleryAlbumEntry != null) {
            for (int a = 0; a < Math.min(100, galleryAlbumEntry.photos.size()); a++) {
                MediaController.PhotoEntry photoEntry = galleryAlbumEntry.photos.get(a);
                photoEntry.reset();
            }
        }
        clearSelectedPhotos();
        updatePhotosCounter(false);
        cameraPhotoLayoutManager.scrollToPositionWithOffset(0, 1000000);
        layoutManager.scrollToPositionWithOffset(0, 1000000);

        dropDown.setText(LocaleController.getString(R.string.ChatGallery));

        selectedAlbumEntry = galleryAlbumEntry;
        if (selectedAlbumEntry != null) {
            loading = false;
            if (progressView != null) {
                progressView.showTextView();
            }
        }
        updateAlbumsDropDown();
    }

    @Override
    public boolean canScheduleMessages() {
        boolean hasTtl = false;
        for (HashMap.Entry<Object, Object> entry : selectedPhotos.entrySet()) {
            Object object = entry.getValue();
            if (object instanceof MediaController.PhotoEntry) {
                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) object;
                if (photoEntry.ttl != 0) {
                    hasTtl = true;
                    break;
                }
            } else if (object instanceof MediaController.SearchImage) {
                MediaController.SearchImage searchImage = (MediaController.SearchImage) object;
                if (searchImage.ttl != 0) {
                    hasTtl = true;
                    break;
                }
            }
        }
        if (hasTtl) {
            return false;
        }
        return true;
    }

    @Override
    public void onButtonsTranslationYUpdated() {
        checkCameraViewPosition();
        invalidate();
    }

    @Override
    public void setTranslationY(float translationY) {
        if (parentAlert.getSheetAnimationType() == 1) {
            float scale = -0.1f * (translationY / 40.0f);
            for (int a = 0, N = gridView.getChildCount(); a < N; a++) {
                View child = gridView.getChildAt(a);
                if (child instanceof PhotoAttachCameraCell) {
                    PhotoAttachCameraCell cell = (PhotoAttachCameraCell) child;
                    cell.getImageView().setScaleX(1.0f + scale);
                    cell.getImageView().setScaleY(1.0f + scale);
                } else if (child instanceof PhotoAttachPhotoCell) {
                    PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) child;
                    cell.getCheckBox().setScaleX(1.0f + scale);
                    cell.getCheckBox().setScaleY(1.0f + scale);
                }
            }
        }
        super.setTranslationY(translationY);
        parentAlert.getSheetContainer().invalidate();
        invalidate();
    }

    @Override
    public void requestLayout() {
        if (ignoreLayout) {
            return;
        }
        super.requestLayout();
    }

    private ViewPropertyAnimator headerAnimator;

    @Override
    public void onShow(ChatAttachAlert.AttachAlertLayout previousLayout) {
        if (headerAnimator != null) {
            headerAnimator.cancel();
        }
        dropDownContainer.setVisibility(VISIBLE);
        if (!(previousLayout instanceof ChatAttachAlertPhotoLayoutPreview)) {
            clearSelectedPhotos();
            dropDown.setAlpha(1);
        } else {
            headerAnimator = dropDown.animate().alpha(1f).setDuration(150).setInterpolator(CubicBezierInterpolator.EASE_BOTH);
            headerAnimator.start();
        }
        parentAlert.actionBar.setTitle("");

        layoutManager.scrollToPositionWithOffset(0, 0);
        if (previousLayout instanceof ChatAttachAlertPhotoLayoutPreview) {
            Runnable setScrollY = () -> {
                int currentItemTop = previousLayout.getCurrentItemTop(),
                        paddingTop = previousLayout.getListTopPadding();
                gridView.scrollBy(0, (currentItemTop > dp(8) ? paddingTop - currentItemTop : paddingTop));
            };
            gridView.post(setScrollY);
        }

        checkCameraViewPosition();

        resumeCameraPreview();
    }

    @Override
    public void onShown() {
        isHidden = false;
        if (cameraView != null) {
            cameraView.setVisibility(VISIBLE);
        }
        if (cameraIcon != null) {
            cameraIcon.setVisibility(VISIBLE);
        }
        if (cameraView != null) {
            int count = gridView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = gridView.getChildAt(a);
                if (child instanceof PhotoAttachCameraCell) {
                    child.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        }
        if (checkCameraWhenShown) {
            checkCameraWhenShown = false;
            checkCamera(true);
        }
    }

    public void setCheckCameraWhenShown(boolean checkCameraWhenShown) {
        this.checkCameraWhenShown = checkCameraWhenShown;
    }

    @Override
    public void onHideShowProgress(float progress) {
        if (cameraView != null) {
            cameraView.setAlpha(progress);
            cameraIcon.setAlpha(progress);
            if (progress != 0 && cameraView.getVisibility() != VISIBLE) {
                cameraView.setVisibility(VISIBLE);
                cameraIcon.setVisibility(VISIBLE);
            } else if (progress == 0 && cameraView.getVisibility() != INVISIBLE) {
                cameraView.setVisibility(INVISIBLE);
                cameraIcon.setVisibility(INVISIBLE);
            }
        }
    }

    @Override
    public void onHide() {
        isHidden = true;
        int count = gridView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = gridView.getChildAt(a);
            if (child instanceof PhotoAttachCameraCell) {
                PhotoAttachCameraCell cell = (PhotoAttachCameraCell) child;
                child.setVisibility(View.VISIBLE);
                saveLastCameraBitmap();
                cell.updateBitmap();
                break;
            }
        }

        if (headerAnimator != null) {
            headerAnimator.cancel();
        }
        headerAnimator = dropDown.animate().alpha(0f).setDuration(150).setInterpolator(CubicBezierInterpolator.EASE_BOTH).withEndAction(() -> dropDownContainer.setVisibility(GONE));
        headerAnimator.start();

        pauseCameraPreview();
    }

    private void pauseCameraPreview() {
        try {
            if (cameraView != null) {
                CameraController.getInstance().stopPreview(cameraView.getCameraSessionObject());
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void resumeCameraPreview() {
        try {
            checkCamera(false);
            if (cameraView != null) {
                CameraController.getInstance().startPreview(cameraView.getCameraSessionObject());
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private void onPhotoEditModeChanged(boolean isEditMode) {
//        if (needCamera && !noCameraPermissions) {
//            if (isEditMode) {
//                if (cameraView != null) {
//                    isCameraFrontfaceBeforeEnteringEditMode = cameraView.isFrontface();
//                    hideCamera(true);
//                }
//            } else {
//                afterCameraInitRunnable = () -> {
//                    pauseCameraPreview();
//                    afterCameraInitRunnable = null;
//                    isCameraFrontfaceBeforeEnteringEditMode = null;
//                };
//                showCamera();
//            }
//        }
    }

    public void pauseCamera(boolean pause) {
        if (needCamera && !noCameraPermissions) {
            if (pause) {
                if (cameraView != null) {
                    isCameraFrontfaceBeforeEnteringEditMode = cameraView.isFrontface();
                    hideCamera(true);
                }
            } else {
//                afterCameraInitRunnable = () -> {
//                    pauseCameraPreview();
//                    afterCameraInitRunnable = null;
//                    isCameraFrontfaceBeforeEnteringEditMode = null;
//                };
                showCamera();
            }
        }
    }

    @Override
    public void onHidden() {
        if (cameraView != null) {
            cameraView.setVisibility(GONE);
            cameraIcon.setVisibility(GONE);
        }
        for (Map.Entry<Object, Object> en : selectedPhotos.entrySet()) {
            if (en.getValue() instanceof MediaController.PhotoEntry) {
                ((MediaController.PhotoEntry) en.getValue()).isAttachSpoilerRevealed = false;
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (lastNotifyWidth != right - left) {
            lastNotifyWidth = right - left;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        super.onLayout(changed, left, top, right, bottom);
        checkCameraViewPosition();
    }

    @Override
    public void onPreMeasure(int availableWidth, int availableHeight) {
        ignoreLayout = true;
        if (AndroidUtilities.isTablet()) {
            itemsPerRow = 4;
        } else if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
            itemsPerRow = 4;
        } else {
            itemsPerRow = 3;
        }
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        layoutParams.topMargin = ActionBar.getCurrentActionBarHeight();

        itemSize = (availableWidth - dp(6 * 2) - dp(5 * 2)) / itemsPerRow;

        if (lastItemSize != itemSize) {
            lastItemSize = itemSize;
            AndroidUtilities.runOnUIThread(() -> adapter.notifyDataSetChanged());
        }

        layoutManager.setSpanCount(Math.max(1, itemSize * itemsPerRow + dp(5) * (itemsPerRow - 1)));
        int rows = (int) Math.ceil((adapter.getItemCount() - 1) / (float) itemsPerRow);
        int contentSize = rows * itemSize + (rows - 1) * dp(5);
        int newSize = Math.max(0, availableHeight - contentSize - ActionBar.getCurrentActionBarHeight() - dp(48 + 12));
        if (gridExtraSpace != newSize) {
            gridExtraSpace = newSize;
            adapter.notifyDataSetChanged();
        }
        int paddingTop;
        if (!AndroidUtilities.isTablet() && AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
            paddingTop = (int) (availableHeight / 3.5f);
        } else {
            paddingTop = (availableHeight / 5 * 2);
        }
        paddingTop -= dp(52);
        if (paddingTop < 0) {
            paddingTop = 0;
        }
        if (gridView.getPaddingTop() != paddingTop) {
            gridView.setPadding(dp(6), paddingTop, dp(6), dp(48));
        }
        dropDown.setTextSize(!AndroidUtilities.isTablet() && AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? 18 : 20);
        ignoreLayout = false;
    }

    @Override
    public boolean canDismissWithTouchOutside() {
        return !cameraOpened;
    }

    @Override
    public void onPanTransitionStart(boolean keyboardVisible, int contentHeight) {
        super.onPanTransitionStart(keyboardVisible, contentHeight);
        checkCameraViewPosition();
        if (cameraView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraView.invalidateOutline();
            } else {
                cameraView.invalidate();
            }
        }
        if (cameraIcon != null) {
            cameraIcon.invalidate();
        }
    }

    @Override
    public void onContainerTranslationUpdated(float currentPanTranslationY) {
        this.currentPanTranslationY = currentPanTranslationY;
        checkCameraViewPosition();
        if (cameraView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraView.invalidateOutline();
            } else {
                cameraView.invalidate();
            }
        }
        if (cameraIcon != null) {
            cameraIcon.invalidate();
        }
        invalidate();
    }

    @Override
    public void onOpenAnimationEnd() {
        checkCamera(parentAlert != null && parentAlert.baseFragment instanceof ChatActivity);
    }

    @Override
    public void onDismissWithButtonClick(int item) {
        hideCamera(item != 0 && item != 2);
    }

    @Override
    public boolean onDismiss() {
        if (cameraAnimationInProgress) {
            return true;
        }
        if (cameraOpened) {
            closeCamera(true);
            return true;
        }
        hideCamera(true);
        return false;
    }

    @Override
    public boolean onSheetKeyDown(int keyCode, KeyEvent event) {
        if (cameraOpened && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) {
            shutterButton.getDelegate().shutterReleased();
            return true;
        }
        return false;
    }

    @Override
    public boolean onContainerViewTouchEvent(MotionEvent event) {
        if (cameraAnimationInProgress) {
            return true;
        } else if (cameraOpened) {
            return processTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onCustomMeasure(View view, int width, int height) {
        boolean isPortrait = width < height;
        if (view == cameraIcon) {
            cameraIcon.measure(View.MeasureSpec.makeMeasureSpec(itemSize, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec((int) (itemSize - cameraViewOffsetBottomY - cameraViewOffsetY), View.MeasureSpec.EXACTLY));
            return true;
        } else if (view == cameraView) {
            if (cameraOpened && !cameraAnimationInProgress) {
                cameraView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height + parentAlert.getBottomInset(), View.MeasureSpec.EXACTLY));
                return true;
            }
        } else if (view == cameraPanel) {
            if (isPortrait) {
                cameraPanel.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(dp(126), View.MeasureSpec.EXACTLY));
            } else {
                cameraPanel.measure(View.MeasureSpec.makeMeasureSpec(dp(126), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            }
            return true;
        } else if (view == zoomControlView) {
            if (isPortrait) {
                zoomControlView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(dp(50), View.MeasureSpec.EXACTLY));
            } else {
                zoomControlView.measure(View.MeasureSpec.makeMeasureSpec(dp(50), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            }
            return true;
        } else if (view == cameraPhotoRecyclerView) {
            cameraPhotoRecyclerViewIgnoreLayout = true;
            if (isPortrait) {
                cameraPhotoRecyclerView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(dp(80), View.MeasureSpec.EXACTLY));
                if (cameraPhotoLayoutManager.getOrientation() != LinearLayoutManager.HORIZONTAL) {
                    cameraPhotoRecyclerView.setPadding(dp(8), 0, dp(8), 0);
                    cameraPhotoLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    cameraAttachAdapter.notifyDataSetChanged();
                }
            } else {
                cameraPhotoRecyclerView.measure(View.MeasureSpec.makeMeasureSpec(dp(80), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                if (cameraPhotoLayoutManager.getOrientation() != LinearLayoutManager.VERTICAL) {
                    cameraPhotoRecyclerView.setPadding(0, dp(8), 0, dp(8));
                    cameraPhotoLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    cameraAttachAdapter.notifyDataSetChanged();
                }
            }
            cameraPhotoRecyclerViewIgnoreLayout = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean onCustomLayout(View view, int left, int top, int right, int bottom) {
        int width = (right - left);
        int height = (bottom - top);
        boolean isPortrait = width < height;
        if (view == cameraPanel) {
            if (isPortrait) {
                if (cameraPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    cameraPanel.layout(0, bottom - dp(126 + 96), width, bottom - dp(96));
                } else {
                    cameraPanel.layout(0, bottom - dp(126), width, bottom);
                }
            } else {
                if (cameraPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    cameraPanel.layout(right - dp(126 + 96), 0, right - dp(96), height);
                } else {
                    cameraPanel.layout(right - dp(126), 0, right, height);
                }
            }
            return true;
        } else if (view == zoomControlView) {
            if (isPortrait) {
                if (cameraPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    zoomControlView.layout(0, bottom - dp(126 + 96 + 38 + 50), width, bottom - dp(126 + 96 + 38));
                } else {
                    zoomControlView.layout(0, bottom - dp(126 + 50), width, bottom - dp(126));
                }
            } else {
                if (cameraPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    zoomControlView.layout(right - dp(126 + 96 + 38 + 50), 0, right - dp(126 + 96 + 38), height);
                } else {
                    zoomControlView.layout(right - dp(126 + 50), 0, right - dp(126), height);
                }
            }
            return true;
        } else if (view == counterTextView) {
            int cx;
            int cy;
            if (isPortrait) {
                cx = (width - counterTextView.getMeasuredWidth()) / 2;
                cy = bottom - dp(113 + 16 + 38);
                counterTextView.setRotation(0);
                if (cameraPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    cy -= dp(96);
                }
            } else {
                cx = right - dp(113 + 16 + 38);
                cy = height / 2 + counterTextView.getMeasuredWidth() / 2;
                counterTextView.setRotation(-90);
                if (cameraPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    cx -= dp(96);
                }
            }
            counterTextView.layout(cx, cy, cx + counterTextView.getMeasuredWidth(), cy + counterTextView.getMeasuredHeight());
            return true;
        } else if (view == cameraPhotoRecyclerView) {
            if (isPortrait) {
                int cy = height - dp(88);
                view.layout(0, cy, view.getMeasuredWidth(), cy + view.getMeasuredHeight());
            } else {
                int cx = left + width - dp(88);
                view.layout(cx, 0, cx + view.getMeasuredWidth(), view.getMeasuredHeight());
            }
            return true;
        }
        return false;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.albumsDidLoad) {
            if (adapter != null) {
                if (shouldLoadAllMedia()) {
                    galleryAlbumEntry = MediaController.allMediaAlbumEntry;
                } else {
                    galleryAlbumEntry = MediaController.allPhotosAlbumEntry;
                }
                if (selectedAlbumEntry == null || parentAlert != null && parentAlert.isStickerMode) {
                    selectedAlbumEntry = galleryAlbumEntry;
                } else if (shouldLoadAllMedia()) {
                    for (int a = 0; a < MediaController.allMediaAlbums.size(); a++) {
                        MediaController.AlbumEntry entry = MediaController.allMediaAlbums.get(a);
                        if (entry.bucketId == selectedAlbumEntry.bucketId && entry.videoOnly == selectedAlbumEntry.videoOnly) {
                            selectedAlbumEntry = entry;
                            break;
                        }
                    }
                }
                loading = false;
                progressView.showTextView();
                adapter.notifyDataSetChanged();
                cameraAttachAdapter.notifyDataSetChanged();
                if (!selectedPhotosOrder.isEmpty() && galleryAlbumEntry != null) {
                    for (int a = 0, N = selectedPhotosOrder.size(); a < N; a++) {
                        Integer imageId = (Integer) selectedPhotosOrder.get(a);
                        Object currentEntry = selectedPhotos.get(imageId);
                        MediaController.PhotoEntry entry = galleryAlbumEntry.photosByIds.get(imageId);
                        if (entry != null) {
                            if (currentEntry instanceof MediaController.PhotoEntry) {
                                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) currentEntry;
                                entry.copyFrom(photoEntry);
                            }
                            selectedPhotos.put(imageId, entry);
                        }
                    }
                }
                updateAlbumsDropDown();
            }
        } else if (id == NotificationCenter.cameraInitied) {
            checkCamera(false);
        }
    }

    private class PhotoAttachAdapter extends RecyclerListView.FastScrollAdapter {

        private Context mContext;
        private boolean needCamera;
        private ArrayList<RecyclerListView.Holder> viewsCache = new ArrayList<>(8);
        private int itemsCount;
        private int photosStartRow;
        private int photosEndRow;

        public PhotoAttachAdapter(Context context, boolean camera) {
            mContext = context;
            needCamera = camera;
        }

        public void createCache() {
            for (int a = 0; a < 8; a++) {
                viewsCache.add(createHolder());
            }
        }

        public RecyclerListView.Holder createHolder() {
            PhotoAttachPhotoCell cell = new PhotoAttachPhotoCell(mContext, resourcesProvider);
            if (Build.VERSION.SDK_INT >= 21 && this == adapter) {
                cell.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        PhotoAttachPhotoCell photoCell = (PhotoAttachPhotoCell) view;
                        if (photoCell.getTag() == null) {
                            return;
                        }
                        int position = (Integer) photoCell.getTag();
                        if (needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                            position++;
                        }
                        if (showAvatarConstructor) {
                            position++;
                        }
                        if (position == 0) {
                            int rad = dp(8 * parentAlert.cornerRadius);
                            outline.setRoundRect(0, 0, view.getMeasuredWidth() + rad, view.getMeasuredHeight() + rad, rad);
                        } else if (position == itemsPerRow - 1) {
                            int rad = dp(8 * parentAlert.cornerRadius);
                            outline.setRoundRect(-rad, 0, view.getMeasuredWidth(), view.getMeasuredHeight() + rad, rad);
                        } else {
                            outline.setRect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                        }
                    }
                });
                cell.setClipToOutline(true);
            }
            cell.setDelegate(v -> {
                if (!mediaEnabled || parentAlert.avatarPicker != 0) {
                    return;
                }
                int index = (Integer) v.getTag();
                MediaController.PhotoEntry photoEntry = v.getPhotoEntry();
                if (checkSendMediaEnabled(photoEntry)) {
                    return;
                }
                if (selectedPhotos.size() + 1 > maxCount()) {
                    BulletinFactory.of(parentAlert.sizeNotifierFrameLayout, resourcesProvider).createErrorBulletin(AndroidUtilities.replaceTags(LocaleController.formatPluralString("BusinessRepliesToastLimit", parentAlert.baseFragment.getMessagesController().quickReplyMessagesLimit))).show();
                    return;
                }
                boolean added = !selectedPhotos.containsKey(photoEntry.imageId);
                if (added && parentAlert.maxSelectedPhotos >= 0 && selectedPhotos.size() >= parentAlert.maxSelectedPhotos) {
                    if (parentAlert.allowOrder && parentAlert.baseFragment instanceof ChatActivity) {
                        ChatActivity chatActivity = (ChatActivity) parentAlert.baseFragment;
                        TLRPC.Chat chat = chatActivity.getCurrentChat();
                        if (chat != null && !ChatObject.hasAdminRights(chat) && chat.slowmode_enabled) {
                            if (alertOnlyOnce != 2) {
                                AlertsCreator.createSimpleAlert(getContext(), LocaleController.getString(R.string.Slowmode), LocaleController.getString(R.string.SlowmodeSelectSendError), resourcesProvider).show();
                                if (alertOnlyOnce == 1) {
                                    alertOnlyOnce = 2;
                                }
                            }
                        }
                    }
                    return;
                }
                int num = added ? selectedPhotosOrder.size() : -1;
                if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                    v.setChecked(num, added, true);
                } else {
                    v.setChecked(-1, added, true);
                }
                addToSelectedPhotos(photoEntry, index);
                int updateIndex = index;
                if (PhotoAttachAdapter.this == cameraAttachAdapter) {
                    if (adapter.needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                        updateIndex++;
                    }
                    adapter.notifyItemChanged(updateIndex);
                } else {
                    cameraAttachAdapter.notifyItemChanged(updateIndex);
                }
                parentAlert.updateCountButton(added ? 1 : 2);
                cell.setHasSpoiler(photoEntry.hasSpoiler);
                cell.setStarsPrice(photoEntry.starsAmount, selectedPhotos.size() > 1);
            });
            return new RecyclerListView.Holder(cell);
        }

        private MediaController.PhotoEntry getPhoto(int position) {
            if (needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                position--;
            }
            return getPhotoEntryAtPosition(position);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                        position--;
                    }
                    if (showAvatarConstructor) {
                        position--;
                    }
                    PhotoAttachPhotoCell cell = (PhotoAttachPhotoCell) holder.itemView;
                    if (this == adapter) {
                        cell.setItemSize(itemSize);
                    } else {
                        cell.setIsVertical(cameraPhotoLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL);
                    }
                    if (parentAlert.avatarPicker != 0 || parentAlert.storyMediaPicker) {
                        cell.getCheckBox().setVisibility(GONE);
                    } else {
                        cell.getCheckBox().setVisibility(VISIBLE);
                    }

                    MediaController.PhotoEntry photoEntry = getPhotoEntryAtPosition(position);
                    if (photoEntry == null) {
                        return;
                    }
                    cell.setPhotoEntry(photoEntry, selectedPhotos.size() > 1, needCamera && selectedAlbumEntry == galleryAlbumEntry, position == getItemCount() - 1);
                    if (parentAlert.baseFragment instanceof ChatActivity && parentAlert.allowOrder) {
                        cell.setChecked(selectedPhotosOrder.indexOf(photoEntry.imageId), selectedPhotos.containsKey(photoEntry.imageId), false);
                    } else {
                        cell.setChecked(-1, selectedPhotos.containsKey(photoEntry.imageId), false);
                    }
                    if (!videoEnabled && photoEntry.isVideo) {
                        cell.setAlpha(0.3f);
                    } else if (!photoEnabled && !photoEntry.isVideo) {
                        cell.setAlpha(0.3f);
                    } else {
                        cell.setAlpha(1f);
                    }
                    cell.getImageView().setTag(position);
                    cell.setTag(position);
                    break;
                }
                case 1: {
                    cameraCell = (PhotoAttachCameraCell) holder.itemView;
                    if (cameraView != null && cameraView.isInited() && !isHidden) {
                        cameraCell.setVisibility(View.INVISIBLE);
                    } else {
                        cameraCell.setVisibility(View.VISIBLE);
                    }
                    cameraCell.setItemSize(itemSize);
                    break;
                }
                case 3: {
                    PhotoAttachPermissionCell cell = (PhotoAttachPermissionCell) holder.itemView;
                    cell.setItemSize(itemSize);
                    cell.setType(needCamera && noCameraPermissions && position == 0 ? 0 : 1);
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerListView.Holder holder;
            switch (viewType) {
                case 0:
                    if (!viewsCache.isEmpty()) {
                        holder = viewsCache.get(0);
                        viewsCache.remove(0);
                    } else {
                        holder = createHolder();
                    }
                    break;
                case 1:
                    cameraCell = new PhotoAttachCameraCell(mContext, resourcesProvider);
                    if (Build.VERSION.SDK_INT >= 21) {
                        cameraCell.setOutlineProvider(new ViewOutlineProvider() {
                            @Override
                            public void getOutline(View view, Outline outline) {
                                int rad = dp(8 * parentAlert.cornerRadius);
                                outline.setRoundRect(0, 0, view.getMeasuredWidth() + rad, view.getMeasuredHeight() + rad, rad);
                            }
                        });
                        cameraCell.setClipToOutline(true);
                    }
                    holder = new RecyclerListView.Holder(cameraCell);
                    break;
                case 2:
                    holder = new RecyclerListView.Holder(new View(mContext) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(gridExtraSpace, MeasureSpec.EXACTLY));
                        }
                    });
                    break;
                case 3:
                default:
                    holder = new RecyclerListView.Holder(new PhotoAttachPermissionCell(mContext, resourcesProvider));
                    break;
                case 4:
                    AvatarConstructorPreviewCell avatarConstructorPreviewCell = new AvatarConstructorPreviewCell(mContext, parentAlert.forUser) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            super.onMeasure(MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY));
                        }
                    };
                    holder = new RecyclerListView.Holder(avatarConstructorPreviewCell);
                    break;
            }
            return holder;
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder.itemView instanceof PhotoAttachCameraCell) {
                PhotoAttachCameraCell cell = (PhotoAttachCameraCell) holder.itemView;
                cell.updateBitmap();
            }
        }

        @Override
        public int getItemCount() {
            if (!mediaEnabled) {
                return 1;
            }
            int count = 0;
            if (needCamera && selectedAlbumEntry == galleryAlbumEntry) {
                count++;
            }
            if (showAvatarConstructor) {
                count++;
            }
            if (noGalleryPermissions && this == adapter) {
                count++;
            }
            photosStartRow = count;
            count += cameraPhotos.size();
            if (selectedAlbumEntry != null) {
                count += selectedAlbumEntry.photos.size();
            }
            photosEndRow = count;
            if (this == adapter) {
                count++;
            }
            return itemsCount = count;
        }

        @Override
        public int getItemViewType(int position) {
            if (!mediaEnabled) {
                return 2;
            }
            int localPosition = position;
            if (needCamera && position == 0 && selectedAlbumEntry == galleryAlbumEntry) {
                if (noCameraPermissions) {
                    return 3;
                } else {
                    return 1;
                }
            }
            if (needCamera) {
                localPosition--;
            }
            if (showAvatarConstructor && localPosition == 0) {
                return VIEW_TYPE_AVATAR_CONSTRUCTOR;
            }
            if (this == adapter && position == itemsCount - 1) {
                return 2;
            } else if (noGalleryPermissions) {
                return 3;
            }
            return 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (this == adapter) {
                progressView.setVisibility(getItemCount() == 1 && selectedAlbumEntry == null || !mediaEnabled ? View.VISIBLE : View.INVISIBLE);
            }
        }

        @Override
        public float getScrollProgress(RecyclerListView listView) {
            int parentCount = itemsPerRow;
            int cellCount = (int) Math.ceil(itemsCount / (float) parentCount);
            if (listView.getChildCount() == 0) {
                return 0;
            }
            int cellHeight = listView.getChildAt(0).getMeasuredHeight();
            View firstChild = listView.getChildAt(0);
            int firstPosition = listView.getChildAdapterPosition(firstChild);
            if (firstPosition < 0) {
                return 0;
            }
            float childTop = firstChild.getTop();
            float listH = listView.getMeasuredHeight();
            float scrollY = (firstPosition / parentCount) * cellHeight - childTop;
            return Utilities.clamp(scrollY / (((float) cellCount) * cellHeight - listH), 1f, 0f);
        }

        @Override
        public String getLetter(int position) {
            MediaController.PhotoEntry entry = getPhoto(position);
            if (entry == null) {
                if (position <= photosStartRow) {
                    if (!cameraPhotos.isEmpty()) {
                        entry = (MediaController.PhotoEntry) cameraPhotos.get(0);
                    } else if (selectedAlbumEntry != null && selectedAlbumEntry.photos != null) {
                        entry = selectedAlbumEntry.photos.get(0);
                    }
                } else if (!selectedAlbumEntry.photos.isEmpty()){
                    entry = selectedAlbumEntry.photos.get(selectedAlbumEntry.photos.size() - 1);
                }
            }
            if (entry != null) {
                long date = entry.dateTaken;
                if (Build.VERSION.SDK_INT <= 28) {
                    date /= 1000;
                }
                return LocaleController.formatYearMont(date, true);
            }
            return "";
        }

        @Override
        public boolean fastScrollIsVisible(RecyclerListView listView) {
            return (!cameraPhotos.isEmpty() || selectedAlbumEntry != null && !selectedAlbumEntry.photos.isEmpty()) && parentAlert.pinnedToTop && getTotalItemsCount() > SHOW_FAST_SCROLL_MIN_COUNT;
        }

        @Override
        public void getPositionForScrollProgress(RecyclerListView listView, float progress, int[] position) {
            int viewHeight = listView.getChildAt(0).getMeasuredHeight();
            int totalHeight = (int) (Math.ceil(getTotalItemsCount() / (float) itemsPerRow) * viewHeight);
            int listHeight = listView.getMeasuredHeight();
            position[0] = (int) ((progress * (totalHeight - listHeight)) / viewHeight) * itemsPerRow;
            position[1] = (int) ((progress * (totalHeight - listHeight)) % viewHeight) + listView.getPaddingTop();
            if (position[0] == 0 && position[1] < getListTopPadding()) {
                position[1] = getListTopPadding();
            }
        }
    }
}
