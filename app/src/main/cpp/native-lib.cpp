#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <omp.h>

using namespace cv;

void Convolute(const Mat &src, Mat& rst);

extern "C" JNIEXPORT void JNICALL
Java_com_example_androidopencvapp_MainActivity_galleryImageToOcv(JNIEnv* env, jobject thiz,
                                                                 jlong mat, jlong matout) {
    Mat& input = *(Mat*) mat;
    Mat& output = *(Mat*) matout;
    CV_Assert(input.depth() == CV_8U);
    Convolute(input, output);
    output.row(0).setTo(Scalar(0));
    output.row(output.rows - 1).setTo(Scalar(0));
    output.col(0).setTo(Scalar(0));
    output.col(output.cols - 1).setTo(Scalar(0));
}

void Convolute(const Mat &src, Mat& rst) {
    try
    {
        const int nchnn = src.channels();
        rst.create(src.size(), src.type());
#pragma omp parallel for
        for (int j = 1; j < src.rows - 1; ++j)
        {
            const uchar* prv = src.ptr<uchar>(j - 1);
            const uchar* cur = src.ptr<uchar>(j);
            const uchar* nxt = src.ptr<uchar>(j + 1);
            uchar* out = rst.ptr<uchar>(j);
            for (int i = nchnn; i < nchnn*(src.cols - 1); ++i)
            {
                uchar gx = saturate_cast<uchar>(prv[i - nchnn] + 2 * cur[i + nchnn] + nxt[i + nchnn] - prv[i - nchnn] - 2 * cur[i - nchnn] - nxt[i - nchnn]);
                uchar gy = saturate_cast<uchar>(nxt[i - nchnn] + 2 * nxt[i] + nxt[i + nchnn] - prv[i - nchnn] - 2 * prv[i] - prv[i + nchnn]);
                *out++ = saturate_cast<uchar>(sqrt(gx * gx + gy * gy));
            }
        }
    }
    catch (const std::exception&)
    {
        throw;
    }
}
