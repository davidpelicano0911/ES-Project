import { useState, useEffect } from "react";
import { getImage } from "../../api/apiPosts";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const PostImage = ({ filename, alt }: { filename: string; alt: string }) => {
  const [imageUrl, setImageUrl] = useState<string>("");
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.PostImage.Load", (span) => {
      span.setAttribute("page.name", "PostImage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    let isMounted = true;

    const loadImage = async () => {
      try {
        const url = await getImage(filename);
        if (isMounted) setImageUrl(url);
      } catch (err) {
        console.error("Failed to load image", err);
      }
    };

    void loadImage();

    return () => {
      isMounted = false;
    };
  }, [filename]);

  if (!imageUrl) return <div className="w-full h-40 bg-gray-200 rounded-lg mb-4" />;

  return <img src={imageUrl} alt={alt} className="w-full h-40 object-cover rounded-lg mb-4" />;
};

export default PostImage;
