import { useState } from 'react';
import { AssistantRuntimeProvider, ThreadPrimitive, ComposerPrimitive } from '@assistant-ui/react';
import { useFlags } from "flagsmith/react";

// ✅ Import Logic & Components



export const MyChat = () => {
  const [isOpen, setIsOpen] = useState(false);
  
  // ✅ Initialize the runtime using your custom hook
  const runtime = useChatApi();
  const flags = useFlags(["enable_chat"]);
  const enableChat = flags.enable_chat.enabled;

  if (!enableChat) return null;

  return (
    <AssistantRuntimeProvider runtime={runtime}>
      
      {/* --- Floating Chat Window --- */}
      {isOpen && (
        <div className="fixed bottom-24 right-6 z-50 w-[380px] max-w-[calc(100vw-2rem)] h-[600px] max-h-[calc(100vh-8rem)] bg-white rounded-2xl shadow-2xl border border-gray-200 overflow-hidden flex flex-col animate-in slide-in-from-bottom-5 fade-in duration-200 font-sans">
          
          {/* Header */}
          <div className="bg-white border-b border-gray-100 p-4 flex justify-between items-center shrink-0 sticky top-0 z-10">
            <div>
              <h2 className="font-bold text-gray-800 text-lg">AI Assistant</h2>
              <p className="text-xs text-blue-600 font-medium">Online</p>
            </div>
            <button onClick={() => setIsOpen(false)} className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 p-2 rounded-full transition-all">
              <CloseIcon />
            </button>
          </div>

          <ThreadPrimitive.Root className="flex-1 flex flex-col overflow-hidden bg-white">
            
            {/* Scrollable Message Area */}
            <ThreadPrimitive.Viewport className="flex-1 overflow-y-auto p-4 space-y-6 scroll-smooth">
              <ThreadPrimitive.Empty>
                <div className="flex flex-col items-center justify-center h-full text-center space-y-4 mt-10">
                  <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center">
                    <ChatIcon className="text-blue-600 w-8 h-8" />
                  </div>
                  <p className="text-gray-500 font-medium">How can I help you today?</p>
                </div>
              </ThreadPrimitive.Empty>

              <ThreadPrimitive.Messages components={{
                UserMessage: UserMessage,
                AssistantMessage: AssistantMessage,
              }} />
            </ThreadPrimitive.Viewport>

            {/* Input Area */}
            <ComposerPrimitive.Root className="p-4 bg-white sticky bottom-0">
              <div className="relative flex items-end gap-2 bg-gray-50 border border-gray-200 rounded-3xl px-4 py-2 transition-all shadow-sm focus-within:border-gray-300 focus-within:bg-white">
                <ComposerPrimitive.Input 
                  className="flex-1 max-h-[120px] bg-transparent border-none resize-none focus:ring-0 focus:outline-none text-sm py-2.5 placeholder:text-gray-400 text-gray-800"
                  placeholder="Message..."
                  rows={1}
                />
                <ComposerPrimitive.Send className="mb-1.5 h-8 w-8 bg-blue-600 text-white rounded-full flex items-center justify-center hover:bg-blue-700 disabled:opacity-50 disabled:hover:bg-blue-600 transition-colors shrink-0 shadow-sm">
                  <SendIcon />
                </ComposerPrimitive.Send>
              </div>
              <div className="text-center mt-2">
                <span className="text-[10px] text-gray-400">AI can make mistakes. Check important info.</span>
              </div>
            </ComposerPrimitive.Root>

          </ThreadPrimitive.Root>
        </div>
      )}

      {/* --- Toggle Button --- */}
      <button 
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-6 right-6 z-50 h-14 w-14 bg-blue-600 text-white rounded-full shadow-lg hover:scale-105 hover:bg-blue-700 transition-all flex items-center justify-center focus:outline-none"
      >
        {isOpen ? <CloseIcon /> : <ChatIcon />}
      </button>

    </AssistantRuntimeProvider>
  );
};

import { MessagePrimitive } from '@assistant-ui/react';
import { useChatApi } from '../api/apiChat';

// --- Message Bubbles ---

export const UserMessage = () => (
  <MessagePrimitive.Root className="flex w-full justify-end">
    <div className="bg-blue-600 text-white px-5 py-2.5 rounded-3xl rounded-tr-sm max-w-[85%] text-[15px] leading-relaxed shadow-md">
      <MessagePrimitive.Content />
    </div>
  </MessagePrimitive.Root>
);

export const AssistantMessage = () => (
  <MessagePrimitive.Root className="flex w-full justify-start gap-3">
    <div className="w-8 h-8 rounded-full bg-blue-50 border border-blue-100 flex items-center justify-center shrink-0">
       <span className="text-[10px] font-bold text-blue-600">AI</span>
    </div>
    <div className="bg-white border border-gray-200 text-gray-800 px-5 py-2.5 rounded-3xl rounded-tl-sm max-w-[85%] text-[15px] leading-relaxed shadow-sm">
      <MessagePrimitive.Content />
    </div>
  </MessagePrimitive.Root>
);

// --- Icons ---

export const SendIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/>
  </svg>
);

export const CloseIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M18 6 6 18"/><path d="m6 6 12 12"/>
  </svg>
);

export const ChatIcon = ({className}: {className?: string}) => (
  <svg className={className} width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
  </svg>
);

export default MyChat;