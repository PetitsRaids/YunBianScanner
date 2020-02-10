#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <iostream>
#include <bitset>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <opencv2/imgproc/types_c.h>

using namespace cv;

typedef struct {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
} argb;

extern "C" {
#define TAG "NDK"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

const int8_t firstNum[10] = {63, 52, 50, 49, 44, 38, 35, 42, 41, 37};
const int8_t leftNum[2][10] = {{13, 25, 19, 61, 35, 49, 47, 59, 55, 11,},
                               {39, 51, 27, 33, 29, 57, 5,  17, 9,  23}};
const int8_t rightNum[10] = {114, 102, 108, 66, 92, 78, 80, 68, 72, 116};
const int8_t blackBarSize[4] = {1, 3, 7, 15};
int8_t codeNumber[13] = {0};
int wrongIndex[12] = {0};
float singleBarWidth = 0.0F;
float makeUpWidth = 0.0F;
int singleBlackBarWidth = 0;
int singleWhiteBarWidth = 0;

extern "C++" {
void transBarcode(int8_t codeNumber[]);
bool checkBarcode(int8_t codeNumber[]);
bool compareWidth2(int8_t codeNumberTemp[]);
void calculateBlackAndWhiteBar(int8_t *barcodeNumber);
int doShift(int8_t *codeNumberTemp, int blinkIndex, int codeIndex);
int8_t getBlackBarBinary(int8_t i);
int8_t getWidth(float width, float singleBarWidth);
}

JNIEXPORT jstring JNICALL Java_com_raids_yunbianscanner_support_Scan_identifyBarcode(JNIEnv *env, jclass thiz, jobject bitmap)
{
    AndroidBitmapInfo info;
    int bitmapGetInfo = AndroidBitmap_getInfo(env, bitmap, &info);
    void *address;
    int result = AndroidBitmap_lockPixels(env, bitmap, &address);
    LOGD("bitmapInfo is %d, and width is %d, height is %d. And result is %d.", bitmapGetInfo,
         info.width, info.height, result);
    if (result != 0) {
        LOGD("FAILED IN address.");
    }
    int width = info.width;
    int height = info.height;
    int scanLine = height / 4;
    argb *pixels = static_cast<argb *>(address);
    int barWidthCount = 0;
    int8_t blinkBarcodeNumber[60];
    argb lastColor = pixels[width * scanLine];
    // 测量黑白条的宽度
    for (scanLine; scanLine < height; scanLine += scanLine) {
        int blinkChange = 0;
        for (int x = 0; x < width; x++) {
            argb color = pixels[width * scanLine + x];
            if (lastColor.red != color.red) {
                blinkBarcodeNumber[blinkChange] = static_cast<int8_t>(barWidthCount);
                blinkChange++;
                lastColor = color;
                barWidthCount = 1;
            } else {
                barWidthCount++;
            }
        }
        for (int i = 0; i <= blinkChange; ++i) {
            LOGD("blinkBarcodeNumber[%d] = %d.", i, blinkBarcodeNumber[i]);
        }
        LOGD("blinkChange is %d.", blinkChange);
        if (compareWidth2(blinkBarcodeNumber))
            break;
    }
    // 把二进制数字转化成对应的条码上的数字
    transBarcode(codeNumber);
    // 检验条码
    std::string code;
    if (checkBarcode(codeNumber)) {
        for (int temp : codeNumber) {
            code.push_back(static_cast<char>(temp + '0'));
        }
    } else {
        code = "";
    }
    LOGD("FUNCTION FINISHED!!");
    return env->NewStringUTF(code.c_str());
}
extern "C++" {
bool compareWidth2(int8_t codeNumberTemp[])
{
    // 区别对待黑白条，提高识别率
    calculateBlackAndWhiteBar(codeNumberTemp);
    LOGD("singleBlackBarWidth = %d and singleWhiteBarWidth = %d.", singleBlackBarWidth,
         singleWhiteBarWidth);
    int currentShift = 0;
    int index = 1;
    int wrongCount = 0;
    bool isSucceed = true;
    if (wrongIndex[0] != 0) {
        int currentI = 0;
        for (int w = 0; w < 12; ++w) {
            index = wrongIndex[w];
            if (index == 0) {
                break;
            }
            currentI = 4 * wrongIndex[w];
            if (wrongIndex[w] > 6) {
                currentI = currentI + 5;
            }
            currentShift = doShift(codeNumberTemp, currentI, index);
            if (currentShift == 7) {
                wrongIndex[w] = 0;
            }
            LOGD("WrongIndex[%d] = %d.", w, wrongIndex[w]);
        }
        for (int w = 0; w < 12; ++w) {
            if (wrongIndex[w] != 0) {
                wrongIndex[wrongCount++] = wrongIndex[w];
                wrongIndex[w] = 0;
            }
        }
        if (wrongCount == 0) {
            isSucceed = 1;
        }
    } else {
        for (int i = 4; i < 57; i++) {
            if (i >= 28 && i <= 32)
                continue;
            currentShift = doShift(codeNumberTemp, i, index);
            LOGD("codeNumber[%d] = %d.", index, codeNumber[index]);
            if (currentShift != 7) {
                wrongIndex[wrongCount] = index;
                LOGD("WrongIndex[%d] = %d.", wrongCount, wrongIndex[wrongCount]);
                wrongCount++;
                isSucceed = false;
            }
            index++;
            i += 3;
        }
    }
    return isSucceed;
}

int8_t getWidth(float width, float singleBarWidth) {
    float quotient = width / singleBarWidth;
    if (quotient < 1.0) {
        quotient = 1.0;
    } else if (quotient > 4.0) {
        quotient = 4.0;
    }
    return static_cast<int8_t>(round(quotient));
}

int doShift(int8_t *codeNumberTemp, int blinkIndex, int codeIndex) {
    int currentShift = 0;
    int shift = 0;
    codeNumber[codeIndex] = 0;
    for (int count = 0; count < 4; count++, blinkIndex++) {
        if (blinkIndex % 2 == 0) {
            shift = getWidth(codeNumberTemp[blinkIndex] - makeUpWidth, singleBarWidth);
            currentShift += shift;
        } else {
            shift = getWidth(codeNumberTemp[blinkIndex] + makeUpWidth, singleBarWidth);
            currentShift += shift;
            codeNumber[codeIndex] =
                    codeNumber[codeIndex] | blackBarSize[shift - 1] << (7 - currentShift);
        }
        LOGD("currentShift = %d, shift = %d.", currentShift, shift);
    }
    return currentShift;
}

int8_t getBlackBarBinary(int8_t i) {
    int width = getWidth(i, singleBlackBarWidth);
    return blackBarSize[width - 1];
}

void calculateBlackAndWhiteBar(int8_t *barcodeNumber) {
    int positionBlackBar[6] = {barcodeNumber[1], barcodeNumber[3],
                               barcodeNumber[29], barcodeNumber[31], barcodeNumber[57],
                               barcodeNumber[59]};
    int positionWhiteBar[5] = {barcodeNumber[2], barcodeNumber[28], barcodeNumber[30],
                               barcodeNumber[32], barcodeNumber[58]};
    int max = 0;
    int maxIndex = 0;
    int min = positionBlackBar[0];
    int minIndex = 0;

    for (int i = 0; i < 6; ++i) {
        if (positionBlackBar[i] >= max) {
            max = positionBlackBar[i];
            maxIndex = i;
        }
        if (positionBlackBar[i] < min) {
            min = positionBlackBar[i];
            minIndex = i;
        }
    }
    positionBlackBar[maxIndex] = positionBlackBar[minIndex] = 0;
    singleBlackBarWidth = static_cast<int>(round(
            ((positionBlackBar[0] + positionBlackBar[1] + positionBlackBar[2]
              + positionBlackBar[3] + positionBlackBar[4] + positionBlackBar[5])) / 4.0));

    max = 0;
    min = positionWhiteBar[0];
    minIndex = 0;
    for (int i = 0; i < 5; ++i) {
        if (positionWhiteBar[i] >= max) {
            max = positionWhiteBar[i];
            maxIndex = i;
        }
        if (positionWhiteBar[i] < min) {
            min = positionWhiteBar[i];
            minIndex = i;
        }
    }
    positionWhiteBar[maxIndex] = positionWhiteBar[minIndex] = 0;
    singleWhiteBarWidth = static_cast<int>(round(
            ((positionWhiteBar[0] + positionWhiteBar[1] + positionWhiteBar[2]
              + positionWhiteBar[3] + positionWhiteBar[4]) / 3.0)));

    singleBarWidth = (singleBlackBarWidth + singleWhiteBarWidth) / 2.0f;
    makeUpWidth = singleBarWidth - singleBlackBarWidth;
//    makeUpWidth = abs(singleBarWidth - singleWhiteBarWidth);
}

void transBarcode(int8_t codeNumber[]) {
    int8_t halfCodeNumber = 6;
    int8_t temp = 0;
    for (int index = 1; index < 13; index++) {
        // 根据奇偶性确定前置符的二进制排列
        if (index < halfCodeNumber + 1) {
            std::bitset<8> first(static_cast<unsigned long long int>(codeNumber[index]));
            if (first.count() % 2 == 1) {
                temp = 1;
                codeNumber[0] = codeNumber[0] | temp << (halfCodeNumber - index);
                for (int8_t i = 0; i < 10; ++i) {
                    if (codeNumber[index] == leftNum[0][i]) {
                        codeNumber[index] = i;
                    }
                }
            } else {
                for (int8_t i = 0; i < 10; ++i) {
                    if (codeNumber[index] == leftNum[1][i]) {
                        codeNumber[index] = i;
                    }
                }
            }
        } else {
            for (int8_t i = 0; i < 10; ++i) {
                if (codeNumber[index] == rightNum[i]) {
                    codeNumber[index] = i;
                }
            }
        }
    }
    // 确定前置符
    for (int8_t i = 0; i < 10; ++i) {
        if (codeNumber[0] == firstNum[i]) {
            codeNumber[0] = i;
        }
    }
}

bool checkBarcode(int8_t codeNumber[]) {
    int product = 0;
    int sum = 0;
    for (int i = 11; i >= 0; i--) {
        if (i % 2 == 0) {
            sum += codeNumber[i];
        } else {
            product += codeNumber[i];
        }
    }
    int plusSum = product * 3 + sum;
    return 10 - (plusSum % 10) == codeNumber[12];
}
}

}
