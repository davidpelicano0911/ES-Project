declare module 'survey-core' {
	// Minimal typings for SurveyJS Model used in this project
	export class Model {
		constructor(json?: any);
		JSON?: any;
		render?: () => void;
	}
	export type SurveyJSON = any;
}

declare module 'survey-creator-react' {
	import { Model } from 'survey-core';
	import * as React from 'react';

	export class SurveyCreator {
		constructor(options?: any);
		JSON: any;
		// add other members as needed
	}

	export const SurveyCreatorComponent: React.FC<{ creator: SurveyCreator }>;
	export default SurveyCreatorComponent;
}

declare module 'survey-react-ui' {
	import { Model } from 'survey-core';
	import * as React from 'react';

	export const Survey: React.FC<{ model: Model }>;
	export default Survey;
}
