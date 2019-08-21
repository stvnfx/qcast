import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.ffmpeg.VideoWriterProfile

class MyMP4Profile : VideoWriterProfile() {
    private var mode = WriterMode.Lossless
    private var constantRateFactor = 23

    enum class WriterMode {
        Normal,
        Lossless
    }

    fun mode(mode: WriterMode): MyMP4Profile {
        this.mode = mode
        return this
    }

    fun constantRateFactor(constantRateFactor: Int): MyMP4Profile {
        this.constantRateFactor = constantRateFactor
        return this
    }

    override fun arguments(): Array<String> {
        return when (mode) {
            WriterMode.Normal -> arrayOf("-pix_fmt", "yuv420p", // this will produce videos that are playable by quicktime
                    "-an", "-vcodec", "libx264", "-crf", "" + constantRateFactor)
            WriterMode.Lossless -> {
                println("lossless mode")
                arrayOf(
//                        "-filter_complex", "[0:a]showwaves=s=1920x1080:mode=line[fg];",
                        "-vcodec", "libx264",
                        "-acodec", "aac",
                        "-hls_list_size", "2", "-hls_init_time", "1", "-hls_time", "1", "-hls_flags", "delete_segments",
//                        "-segment_wrap", "24", "-segment_time", "10",
//                        "-segment_list_size", "10", "-hls_playlist_type", "event",
                        "-f", "hls",
//                        "-hls_list_size" ,"20",
//                        "-hls_init_time", "10",
//                        "-hls_time", "10",
//                        "-hls_playlist_type", "vod",
//                        "-segment_list", "playlist.m3u8",
                        "-segment_list_flags", "+live"
//                        "-f", "lavfi", "-i", "aevalsrc=0"
//                        "-hls_flags", "delete_segments"
                )

            }
        }
    }
}

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        val videoWriter = VideoWriter.create().output("playlist.m3u8").profile(MyMP4Profile()).size(width, height).start()

        val image = loadImage("file:data/images/pm5544.png")
        val font = FontImageMap.fromUrl("file:data/fonts/IBMPlexMono-Regular.ttf", 64.0)

        val videoTarget = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }

//        var frame = 0

        extend {
//            videoWriter.start();
            drawer.isolatedWithTarget(videoTarget) {
                drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))
                image(image)

                fill = ColorRGBa.BLUE
                circle(Math.sin(seconds) * width / 2.0 + width / 2.0, Math.sin(0.5 * seconds) * height / 2.0 + height / 2.0, 140.0)
                circle(Math.cos(seconds) * width / 2.0 + width / 2.0, Math.sin(0.5 * seconds) * height / 2.0 + height / 2.0, 140.0)

                fontMap = font
                fill = ColorRGBa.WHITE
                text("CAST", width / 2.0, height / 2.0)
            }

            videoWriter.frame(videoTarget.colorBuffer(0))
            drawer.image(videoTarget.colorBuffer(0))
//            frame++
//            if (frame == 100) {
//                videoWriter.stop()
//                application.exit()
//            }

        }
    }
}