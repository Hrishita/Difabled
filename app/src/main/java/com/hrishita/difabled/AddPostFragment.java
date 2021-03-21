package com.hrishita.difabled;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddPostFragment extends Fragment {
    Spinner spinner;
    ImageView close;
    GridView gridView;
    ImageView mainImage;
    private HomeActivityInterface mInterface;
    View draggable;
    ImageView next;

    private String currentDirectory = Constants.ROOT_DIR;
    ArrayList<DirectoryModel> arrayList = new ArrayList<>();
    ArrayList<DirectoryModel> directories = new ArrayList<>();

    DirectoryAdapter adapter;
    ImageGridAdapter imageGridAdapter;

    boolean isDragging = false;
    int parentHeight;
    float draggableY;
    boolean isOnTop = false;


    private BottomSheetBehavior mBottomSheetBehviour;
    private CoordinatorLayout parentLayout;
    private RelativeLayout relativeLayoutAppBar;

    private HashMap<String, Boolean> selectedImages = new HashMap<>();

    public AddPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof HomeActivityInterface)
        {
            mInterface = (HomeActivityInterface) context;
            mInterface.hideAppBar();
            mInterface.hideBottomNav();
        }
        else{
            throw new ClassCastException("Implement Interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_post, container, false);
        parentHeight = getResources().getDisplayMetrics().heightPixels;
        initViews(v);
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(R.layout.loading)
                .setCancelable(false)
                .create();
        dialog.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                loadDirectories();
                try {
                    Thread.sleep(3000);
                    ((HomeActivity)getContext())
                            .runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
//                                    CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
//                                    params.height = h;
                                    mBottomSheetBehviour.setPeekHeight(h);
//                                    draggable.setLayoutParams(params);
                                    mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

                                }
                            });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((HomeActivity)getContext())
                        .runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new DirectoryAdapter(getContext(), directories);
                                spinner.setAdapter(adapter);
                            }
                        });
            }
        });
        t.start();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadImages(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;

    }

    private void loadImages(DirectoryModel parentDirectoryModel) {
        ArrayList<DirectoryModel> files = new ArrayList<>();
        for(int i = 0;i < arrayList.size();i++)
        {
            if(arrayList.get(i).directory.equals(parentDirectoryModel.directory))
            {
                files.add(arrayList.get(i));
            }
        }
        imageGridAdapter = new ImageGridAdapter(getContext(), files, new ImageSelectionCallback() {
            @Override
            public void onImageSelectionChange(boolean isSelected, String path) {
                selectedImages.put(path, isSelected);
            }
        }, selectedImages);
        gridView.setAdapter(imageGridAdapter);
    }

    private void loadDirectories() {
        getFile(new File(Constants.ROOT_DIR));

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews(View v) {
        spinner = v.findViewById(R.id.spinner_add_post);
        gridView = v.findViewById(R.id.frag_add_post_grid);
        mainImage = v.findViewById(R.id.frag_add_post_img);
        close = v.findViewById(R.id.img_add_post_close);
        next = v.findViewById(R.id.add_post_next);
        draggable = v.findViewById(R.id.rl_add_post_draggable);
        mBottomSheetBehviour = BottomSheetBehavior.from(draggable);
        relativeLayoutAppBar = v.findViewById(R.id.add_post_app_bar);
        parentLayout = v.findViewById(R.id.rl_add_post_parent);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCaptionPostFragment fragment = new AddCaptionPostFragment();
                Bundle bundle = new Bundle();
                ArrayList<String> arrayList = new ArrayList<>();

                for(String s : selectedImages.keySet())
                {
                    if(selectedImages.get(s))
                        arrayList.add(s);
                }

                bundle.putStringArrayList("selectedimages", arrayList);
                fragment.setArguments(bundle);

                ((HomeActivity)getContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_activity_base, fragment, "caption")
                        .commit();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo close this fragment
            }
        });

//        parentLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("parent layout post");
//                System.out.println("calc height" + parentLayout.getHeight());
//                System.out.println("calc height relk" + relativeLayoutAppBar.getHeight());
//                System.out.println("calc height dr a" + draggable.getHeight());
//                System.out.println("toal" + (relativeLayoutAppBar.getHeight() + draggable.getHeight()));
//
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
////                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
////                params.height = h;
//
//                mBottomSheetBehviour.setPeekHeight(h);
////                draggable.setLayoutParams(params);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            }
//        });
//        draggable.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
////                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
////                params.height = h;
//                System.out.println("calc height" + parentLayout.getHeight());
//                System.out.println("calc height relk" + relativeLayoutAppBar.getHeight());
//                System.out.println("calc height dr a" + draggable.getHeight());
//
//                System.out.println("toal" + (relativeLayoutAppBar.getHeight() + draggable.getHeight()));
//
//                mBottomSheetBehviour.setPeekHeight(h);
////                draggable.setLayoutParams(params);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                ViewTreeObserver obs = draggable.getViewTreeObserver();
//
//                obs.removeOnGlobalLayoutListener(this);
//            }
//        });
//        relativeLayoutAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
//                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
//                params.height = h;
//                System.out.println("Relative layout tree");
//                System.out.println("Relativecalc height" + parentLayout.getHeight());
//                System.out.println("Relativecalc height relk" + relativeLayoutAppBar.getHeight());
//                System.out.println("Relativecalc height dr a" + draggable.getHeight());
//
//                System.out.println("toal" + (relativeLayoutAppBar.getHeight() + draggable.getHeight()));
//
//                mBottomSheetBehviour.setPeekHeight(h);
//                draggable.setLayoutParams(params);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                ViewTreeObserver obs = draggable.getViewTreeObserver();
//
//                obs.removeOnGlobalLayoutListener(this);
//            }
//        });
//        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                System.out.println("parne lay tree");
//
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
////                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
////                params.height = h;
//                System.out.println("parne lay treecalc height" + parentLayout.getHeight());
//                System.out.println("parne lay treecalc height relk" + relativeLayoutAppBar.getHeight());
//                System.out.println("parne lay treecalc height dr a" + draggable.getHeight());
//
//                System.out.println("toal" + (relativeLayoutAppBar.getHeight() + draggable.getHeight()));
//
//                mBottomSheetBehviour.setPeekHeight(h);
////                draggable.setLayoutParams(params);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                ViewTreeObserver obs = draggable.getViewTreeObserver();
//
//                obs.removeOnGlobalLayoutListener(this);
//            }
//        });

//        relativeLayoutAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
//                draggable.setMinimumHeight(h);
////                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
////                params.height = h;
//                System.out.println("calc height" + h);
//                mBottomSheetBehviour.setPeekHeight(h);
////                draggable.setLayoutParams(params);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            }
//        });

//        parentLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
//                draggable.setMinimumHeight(h);
////                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
////                params.height = h;
//                System.out.println("calc height" + h);
//                mBottomSheetBehviour.setPeekHeight(h);
////                draggable.setLayoutParams(params);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
//
//            }
//        });
//
//        relativeLayoutAppBar.post(new Runnable() {
//            @Override
//            public void run() {
//                int h = parentLayout.getHeight() - relativeLayoutAppBar.getHeight();
////                draggable.setMinimumHeight(h);
////                CoordinatorLayout.LayoutParams params= (CoordinatorLayout.LayoutParams) draggable.getLayoutParams();
////                params.height = h;
//                System.out.println("calc height" + h);
//
//                mBottomSheetBehviour.setPeekHeight(h);
//                mBottomSheetBehviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
////                draggable.setLayoutParams(params);
//            }
//        });

//        mainImage.post(new Runnable() {
//            @Override
//            public void run() {
//                draggable.setY(mainImage.getY() + mainImage.getHeight());
//                int height= (int) (parent.getHeight() - (mainImage.getY() + mainImage.getHeight()) + draggable.getY());
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//                draggable.setLayoutParams(params);
////                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////                params.addRule(RelativeLayout.BELOW, mainImage.getId());
////                draggable.setLayoutParams(params);
////                RelativeLayout.LayoutParams p = ((RelativeLayout.LayoutParams)gridView.getLayoutParams());
////                gridView.setY(mainImage.getY() + mainImage.getHeight());
//                isOnTop = false;
//            }
//        });
//        draggable.setClickable(true);
//        draggable.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN)
//                {
//                    //grabbed
//                    isDragging  = true;
//                    draggableY = event.getRawY();
//                }
//                if(event.getAction() == MotionEvent.ACTION_MOVE)
//                {
//                    //swiped
//                    int height= parentHeight - mainImage.getBottom();
//                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//                    draggable.setLayoutParams(params);
//
//                    float offset = draggableY - event.getRawY();
//                    draggableY = event.getRawY();
//                    if(draggable.getY() - offset < parentHeight)
//                    {
//                        draggable.setY(draggable.getY() - offset);
//                    }
//                }
//                if(event.getAction() == MotionEvent.ACTION_UP)
//                {
//                    //released
//                    float offset = draggableY - event.getRawY();
//                    float thresh;
//                    if(!isOnTop)
//                    {
//                        thresh = 2f;
//                    }
//                    else
//                    {
//                        thresh = 6f;
//                    }
//                    final float oldY = draggable.getY();
//                    if(draggable.getY() <  (parentHeight / thresh))
//                    {
//                        ValueAnimator animator = ValueAnimator.ofFloat(draggable.getY(),0);
//                        animator.start();
//                        animator.setDuration(500);
//                        animator.addListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animation) {
////                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
////                                draggable.setLayoutParams(params);
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animation) {
//
//                            }
//                        });
//                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator animation) {
//
//                                int height= parentHeight - mainImage.getBottom();
//                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//                                draggable.setLayoutParams(params);
//                                draggable.setY((Float) animation.getAnimatedValue());
//                                isOnTop=  true;
//                            }
//                        });
//                    }
//                    else
//                    {
//                        isOnTop=  false;
//
//                        ValueAnimator animator = ValueAnimator.ofFloat(draggable.getY(),(mainImage.getY() + mainImage.getHeight()));
//                        animator.start();
//                        animator.setDuration(500);
//                        animator.addListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animation) {
////                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////                                params.addRule(RelativeLayout.BELOW, mainImage.getId());
////                                draggable.setLayoutParams(params);
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//
////                                draggable.setY(oldY);
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animation) {
//
//                            }
//                        });
//                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator animation) {
//                                int height= parentHeight - mainImage.getBottom();
//                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//                                draggable.setLayoutParams(params);
//                                draggable.setY((Float) animation.getAnimatedValue());
//                            }
//                        });
//
//                    }
//                    isDragging = false;
//                }
//
//                return false;
//            }
//        });

//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth/3;
//        gridView.setColumnWidth(imageWidth);

    }

    private ArrayList<String> getDirectoryPaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File f = new File(directory);
        File[] fileList = f.listFiles();
        if (fileList != null) {
            for(int i = 0;i < fileList.length;i++)
            {
                if(fileList[i].isDirectory())
                {
                    pathArray.add(fileList[i].getPath());
                }
            }
        }
        return pathArray;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInterface.showBottomNav();
        mInterface.showAppBar();
    }

    private ArrayList<String> getFileInDirectory(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File f = new File(directory);
        File[] fileList = f.listFiles();
        if (fileList != null) {
            for(int i = 0;i < fileList.length;i++)
            {
                if(fileList[i].isFile())
                {
                    pathArray.add(fileList[i].getPath());
                }
            }
        }
        return pathArray;
    }

    private void getFile(File dir) {
        File[] listFile = dir.listFiles();
        boolean isDirectoryAdded = false;
        if (listFile != null && listFile.length > 0) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    getFile(file);
                }
                else {
                    if (file.getName().endsWith(".png")
                            || file.getName().endsWith(".jpg")
                            || file.getName().endsWith(".jpeg"))
//                            || file.getName().endsWith(".gif")
//                            || file.getName().endsWith(".bmp")
//                            || file.getName().endsWith(".webp"))
                    {
                        //file found in folder so add file in array
                        if(!isDirectoryAdded)
                        {
                            DirectoryModel model = new DirectoryModel();
                            model.directory = file.getParent();
                            model.image = null;
                            model.displayName= file.getParentFile().getName();
                            directories.add(model);
                            isDirectoryAdded = true;
                        }
                        DirectoryModel model = new DirectoryModel();
                        model.directory = file.getParent();
                        model.image = file.getPath();
                        model.displayName= file.getParentFile().getName();
                        arrayList.add(model);
//                        String temp = file.getPath().substring(0, file.getPath().lastIndexOf('/'));
//                        if (!fileList.contains(temp))
//                            fileList.add(temp);
                    }
                }
            }
        }
        else
        {
        }
    }

    //model for storing image path, directory name and directory path
    private class DirectoryModel
    {
        String displayName;
        String directory;
        String image;
    }

    private class DirectoryAdapter extends BaseAdapter
    {
        private Context context;
        private ArrayList<DirectoryModel> arrayList;

        DirectoryAdapter(Context context, ArrayList<DirectoryModel> arrayList)
        {
            this.context = context;
            this.arrayList = arrayList;
        }
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v= LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            TextView textView = v.findViewById(android.R.id.text1);
            textView.setText(arrayList.get(position).displayName);
            textView.setTextColor(getResources().getColor(R.color.colorDarkInput));
            return v;
        }
    }

    private class ImageGridAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<DirectoryModel> arrayList;
        private Map<String, Boolean> selectedImages = new HashMap<>();
        private ImageSelectionCallback mInterface;


        ImageGridAdapter(Context context, ArrayList<DirectoryModel> arrayList, ImageSelectionCallback mCallback, HashMap<String, Boolean> selectedImages)
        {
            this.context = context;
            this.arrayList = arrayList;
            this.mInterface = mCallback;
            this.selectedImages = selectedImages;
        }
        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.custom_gallery_grid, parent, false);
            ImageView imageView = v.findViewById(R.id.image_gallery_grid);
            RadioButton checkBox = v.findViewById(R.id.checkbox_gallery_grid);
            if(arrayList.get(position) != null && arrayList.get(position).image!=null && arrayList.get(position).directory!=null)
            {
                if(selectedImages.containsKey(arrayList.get(position).directory + arrayList.get(position).image))
                    checkBox.setChecked(selectedImages.get(arrayList.get(position).image));
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mInterface.onImageSelectionChange(isChecked, arrayList.get(position).image);
                        selectedImages.put(arrayList.get(position).image, isChecked);
                    }
                });
            }

            if(imageView==null){

            }
            else {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso
                                .get()
                                .load("file://" + arrayList.get(position).image)
                                .into(mainImage);
                    }
                });

                Glide
                        .with(context)
                        .load(new File(arrayList.get(position).image))
                        .fitCenter()
                        .into(imageView);

//                Picasso.get()
//                        .load("file://" + arrayList.get(position).image)
//                        .error(R.drawable.default_profile)
//                        .into(imageView);

            }
            return v;
        }
    }
    public interface ImageSelectionCallback
    {
        void onImageSelectionChange(boolean isSelected, String path);
    }
}
