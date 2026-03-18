/**
 * Utility functions for extracting and managing tracking metadata from URL parameters
 * Allows forms and landing pages to track user context (email, campaign, source, etc.)
 */

export interface TrackingMetadata {
  email?: string;
  campaign?: string;
  source?: string;
  utm_source?: string;
  utm_medium?: string;
  utm_campaign?: string;
  utm_content?: string;
  utm_term?: string;
  lead_id?: string;
  user_id?: string;
  [key: string]: string | undefined;
}

/**
 * Extract tracking metadata from URL search parameters
 * Supports standard UTM parameters and custom tracking parameters
 * 
 * Examples:
 * ?email=user@example.com&campaign=summer_sale
 * ?utm_source=email&utm_campaign=newsletter
 * ?lead_id=123&source=linkedin
 */
export const extractTrackingMetadata = (): TrackingMetadata => {
  const params = new URLSearchParams(window.location.search);
  const metadata: TrackingMetadata = {};

  // Standard tracking parameters
  const trackingParams = [
    'email',
    'campaign',
    'source',
    'utm_source',
    'utm_medium',
    'utm_campaign',
    'utm_content',
    'utm_term',
    'lead_id',
    'user_id'
  ];

  trackingParams.forEach(param => {
    const value = params.get(param);
    if (value) {
      metadata[param] = value;
    }
  });

  // Capture any additional custom parameters (those not in trackingParams)
  params.forEach((value, key) => {
    if (!trackingParams.includes(key) && key.startsWith('utm_') || key.startsWith('track_')) {
      metadata[key] = value;
    }
  });

  return metadata;
};

/**
 * Store tracking metadata in sessionStorage for access across pages
 */
export const storeTrackingMetadata = (metadata: TrackingMetadata): void => {
  sessionStorage.setItem('trackingMetadata', JSON.stringify(metadata));
};

/**
 * Retrieve tracking metadata from sessionStorage
 */
export const getStoredTrackingMetadata = (): TrackingMetadata => {
  const stored = sessionStorage.getItem('trackingMetadata');
  return stored ? JSON.parse(stored) : {};
};

/**
 * Clear tracking metadata from sessionStorage
 */
export const clearTrackingMetadata = (): void => {
  sessionStorage.removeItem('trackingMetadata');
};

/**
 * Get a URL string with metadata parameters appended
 * Useful for nested links within forms or landing pages
 */
export const appendMetadataToUrl = (url: string, metadata: TrackingMetadata = {}): string => {
  if (Object.keys(metadata).length === 0) {
    metadata = extractTrackingMetadata();
  }

  const urlObj = new URL(url, window.location.origin);
  
  Object.entries(metadata).forEach(([key, value]) => {
    if (value) {
      urlObj.searchParams.set(key, value);
    }
  });

  return urlObj.toString();
};

/**
 * Format metadata for logging/debugging
 */
export const formatMetadataForLogging = (metadata: TrackingMetadata): string => {
  const entries = Object.entries(metadata)
    .filter(([, value]) => value !== undefined)
    .map(([key, value]) => `${key}=${value}`)
    .join('&');
  
  return entries || 'No tracking metadata';
};
