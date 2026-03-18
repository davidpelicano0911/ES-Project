import { useState } from 'react';
import { useLocalRuntime, type ChatModelAdapter } from '@assistant-ui/react';
import api from './apiConfig'; // Your existing Axios config

export const useChatApi = () => {
  const [conversationId, setConversationId] = useState<string | undefined>(undefined);

  const adapter: ChatModelAdapter = {
    async run({ messages, abortSignal }) {
      try {
        // 1. Extract the last user message
        const lastMessage = messages[messages.length - 1];
        const question = lastMessage.content[0].type === 'text' 
          ? lastMessage.content[0].text 
          : '';

        // 2. Prepare the payload (Object, not string)
        const payload = {
          question,
          ...(conversationId && { conversationId })
        };

        // 3. Send to Backend
        const response = await api.post("/ai/ask", payload, {
          signal: abortSignal,
        });

        const data = response.data;

        // 4. Update Conversation ID if returned
        if (data.conversationId) {
          setConversationId(data.conversationId);
        }

        // 5. Return Answer to UI
        return {
          content: [{ type: 'text', text: data.answer }],
        };
      } catch (error) {
        console.error("Chat API Error:", error);
        return {
          content: [{ type: 'text', text: "Sorry, I'm having trouble connecting to the server." }],
        };
      }
    },
  };

  // Return the initialized runtime
  return useLocalRuntime(adapter);
};
