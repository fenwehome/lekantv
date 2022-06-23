package com.pxf.fftv.plus.model.video.ok;

import android.util.Log;

import com.pxf.fftv.plus.Const;
import com.pxf.fftv.plus.common.CommonUtils;
import com.pxf.fftv.plus.model.IVideoEngine;
import com.pxf.fftv.plus.model.VideoEngineParam;
import com.pxf.fftv.plus.model.video.Video;
import com.pxf.fftv.plus.model.video.cms.CMSVideoEngine;
import com.pxf.fftv.plus.model.video.weiduo.WeiduoVideoBean;

import java.util.ArrayList;

import okhttp3.Request;
import okhttp3.Response;

import static com.pxf.fftv.plus.Const.LOG_TAG;

public class OkVideoEngine implements IVideoEngine {

    private volatile static OkVideoEngine mInstance;

    private OkVideoEngine() {
    }

    public static OkVideoEngine getInstance() {
        if (mInstance == null) {
            synchronized (OkVideoEngine.class) {
                if (mInstance == null) {
                    mInstance = new OkVideoEngine();
                }
            }
        }
        return mInstance;
    }

    private static final String BASE_URL = "https://api.iokzy.com/inc/feifei3/index.php?cid=";

    @Override
    public ArrayList<Video> getVideos(Const.VideoType type, int page) {
        ArrayList<Video> videos = new ArrayList<>();
        String url = "";
        switch (type) {
            // 电影
            case MOVIE_LATEST:
                url = BASE_URL + "5&p=" + page;
                break;
            case MOVIE_ACTION:
                url = BASE_URL + "5&p=" + page;
                break;
            case MOVIE_COMEDY:
                url = BASE_URL + "6&p=" + page;
                break;
            case MOVIE_LOVE:
                url = BASE_URL + "7&p=" + page;
                break;
            case MOVIE_SCARY:
                url = BASE_URL + "9&p=" + page;
                break;
            case MOVIE_SCIENCE:
                url = BASE_URL + "8&p=" + page;
                break;
            case MOVIE_STORY:
                url = BASE_URL + "10&p=" + page;
                break;
            case MOVIE_WAR:
                url = BASE_URL + "11&p=" + page;
                break;
            // 电视剧
            case TELEPLAY_LATEST:
                url = BASE_URL + "12&p=" + page;
                break;
            case TELEPLAY_EA:
                url = BASE_URL + "15&p=" + page;
                break;
            case TELEPLAY_CHINA:
                url = BASE_URL + "12&p=" + page;
                break;
            case TELEPLAY_JAPAN:
                url = BASE_URL + "17&p=" + page;
                break;
            case TELEPLAY_KOREA:
                url = BASE_URL + "14&p=" + page;
                break;
            case TELEPLAY_OTHER:
                url = BASE_URL + "18&p=" + page;
                break;
            case TELEPLAY_TAIWAN:
                url = BASE_URL + "16&p=" + page;
                break;
            case TELEPLAY_HONGKONG:
                url = BASE_URL + "13&p=" + page;
                break;
            // 动漫
            case CARTOON_LATEST:
                url = BASE_URL + "23&p=" + page;
                break;
            case CARTOON_EA:
                url = BASE_URL + "25&p=" + page;
                break;
            case CARTOON_JK:
                url = BASE_URL + "24&p=" + page;
                break;
            case CARTOON_CHINA:
                url = BASE_URL + "23&p=" + page;
                break;
            case CARTOON_OTHER:
                url = BASE_URL + "32&p=" + page;
                break;
            // 综艺
            case SHOW_LATEST:
                url = BASE_URL + "26&p=" + page;
                break;
            case SHOW_CHINA:
                url = BASE_URL + "26&p=" + page;
                break;
            case SHOW_EA:
                url = BASE_URL + "29&p=" + page;
                break;
            case SHOW_HT:
                url = BASE_URL + "31&p=" + page;
                break;
            case SHOW_JK:
                url = BASE_URL + "28&p=" + page;
                break;
        }

        Request request = new Request.Builder().url(url).build();

        try {
            Response response = CommonUtils.getOkHttpClient().newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                OkVideoBean bean = CommonUtils.getGson().fromJson(response.body().string(), OkVideoBean.class);
                if (bean.getStatus() == 200) {
                    if (bean.getData() != null && bean.getData() != null) {
                        for (int i = 0; i < bean.getData().size(); i++) {
                            Video video = new Video();
                            OkVideoBean.DataBean item = bean.getData().get(i);

                            video.setType(type);

                            video.setPageItemNum(Integer.parseInt(bean.getPage().getPagesize()));
                            video.setPageCount(Integer.parseInt(bean.getPage().getPagecount()));
                            video.setTitle(item.getVod_name());
                            video.setImageUrl(item.getVod_pic());
                            video.setYear(item.getVod_year());
                            video.setTypeText(item.getList_name());

                            ArrayList<Video.Actor> actors = new ArrayList<>();
                            String[] stars = item.getVod_actor().split(" / ");
                            for (String s : stars) {
                                Video.Actor actor = new Video.Actor();
                                actor.setName(s);
                                actors.add(actor);
                            }
                            video.setActors(actors);

                            ArrayList<Video.Director> directors = new ArrayList<>();
                            Video.Director director = new Video.Director();
                            director.setName(item.getVod_director());
                            directors.add(director);
                            video.setDirectors(directors);

                            ArrayList<Video.Part> parts = new ArrayList<>();
                            // 不同资源
                            String[] resources = item.getVod_url().split("\\$\\$\\$");
                            // 不同的集数，OK资源网的m3u8播放链接在数组第2位，所以先判断数组长度是否有2位
                            String[] sets;
                            if (resources.length <= 1) {
                                sets = resources[0].split("\r\n");
                            } else {
                                sets = resources[1].split("\r\n");
                            }
                            for (int j = 0; j < sets.length; j++) {
                                String[] urlArray = sets[j].split("\\$");
                                if (urlArray.length == 2) {
                                    Video.Part part = new Video.Part();
                                    part.setTitle(urlArray[0]);
                                    part.setUrl(urlArray[1]);
                                    parts.add(part);
                                }
                            }

                            video.setParts(parts);
                            videos.add(video);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getVideos error ", e);
            return getVideos(type, page + 1);
        }

        return videos;
    }

    @Override
    public ArrayList<Video> getVideos(VideoEngineParam param, int page) {
        ArrayList<Video> videos = new ArrayList<>();
        String url = param.getUrl() + page;

        Request request = new Request.Builder().url(url).build();

        try {
            Response response = CommonUtils.getOkHttpClient().newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                try {
                    OkVideoBean bean = CommonUtils.getGson().fromJson(response.body().string(), OkVideoBean.class);
                    if (bean.getStatus() == 200) {
                        if (bean.getData() != null && bean.getData() != null) {
                            for (int i = 0; i < bean.getData().size(); i++) {
                                Video video = new Video();
                                OkVideoBean.DataBean item = bean.getData().get(i);

                                video.setVideoEngineParam(param);

                                video.setPageItemNum(Integer.parseInt(bean.getPage().getPagesize()));
                                video.setPageCount(Integer.parseInt(bean.getPage().getPagecount()));
                                video.setTitle(item.getVod_name());
                                video.setImageUrl(item.getVod_pic());
                                video.setYear(item.getVod_year());
                                video.setTypeText(item.getList_name());

                                ArrayList<Video.Actor> actors = new ArrayList<>();
                                String[] stars = item.getVod_actor().split(" / ");
                                for (String s : stars) {
                                    Video.Actor actor = new Video.Actor();
                                    actor.setName(s);
                                    actors.add(actor);
                                }
                                video.setActors(actors);

                                ArrayList<Video.Director> directors = new ArrayList<>();
                                Video.Director director = new Video.Director();
                                director.setName(item.getVod_director());
                                directors.add(director);
                                video.setDirectors(directors);

                                ArrayList<Video.Part> parts = new ArrayList<>();
                                // 不同资源
                                String[] resources = item.getVod_url().split("\\$\\$\\$");
                                // 不同的集数，OK资源网的m3u8播放链接在数组第2位，所以先判断数组长度是否有2位
                                String[] sets;
                                if (resources.length <= 1) {
                                    sets = resources[0].split("\r\n");
                                } else {
                                    sets = resources[1].split("\r\n");
                                }
                                for (int j = 0; j < sets.length; j++) {
                                    String[] urlArray = sets[j].split("\\$");
                                    if (urlArray.length == 2) {
                                        Video.Part part = new Video.Part();
                                        part.setTitle(urlArray[0]);
                                        part.setUrl(urlArray[1]);
                                        parts.add(part);
                                    }
                                }

                                video.setParts(parts);
                                videos.add(video);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "getVideos error ", e);
        }

        return videos;
    }
}
