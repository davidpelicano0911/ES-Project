import EmailTemplateCard from "./EmailTemplateCard";
import FormTemplateCard from "./FormTemplateCard";
import LandingPageCard from "./LandingPageCard";
import SocialPostCard from "./SocialPostCard";

import { 
  materialToEmailTemplate, 
  materialToFormTemplate, 
  materialToLandingPage,
  materialToPost 
} from "../../utils/materialMappers";

import type { Material } from "../../api/apiMaterials";

interface MaterialCardRendererProps {
  material: Material;
  onView: (material: Material) => void;
  onDelete?: (material: Material) => void;
  onRemove?: (material: Material) => void;
}

const MaterialCardRenderer = ({ material, onView, onDelete, onRemove }: MaterialCardRendererProps) => {
  switch (material.type) {
    case "EMAIL":
      return (
        <EmailTemplateCard
          template={materialToEmailTemplate(material)}
          onView={() => onView(material)}
          onDelete={onDelete ? () => onDelete(material) : undefined}
          onRemove={onRemove ? () => onRemove(material) : undefined}
          isInCampaign={true}
        />
      );

    case "FORM":
      return (
        <FormTemplateCard
          template={materialToFormTemplate(material)}
          onView={() => onView(material)}
          onDelete={onDelete ? () => onDelete(material) : undefined}
          onRemove={onRemove ? () => onRemove(material) : undefined}
          isInCampaign={true}
        />
      );

    case "LANDING_PAGE":
      return (
        <LandingPageCard
          page={materialToLandingPage(material)}
          onView={() => onView(material)}
          onDelete={onDelete ? () => onDelete(material) : undefined}
          onRemove={onRemove ? () => onRemove(material) : undefined}
          isInCampaign={true}
        />
      );

    case "POST":
      return (
        <SocialPostCard
          post={materialToPost(material)}
          onView={() => onView(material)}
          onDelete={onDelete ? () => onDelete(material) : undefined}
          onRemove={onRemove ? () => onRemove(material) : undefined}
          isInCampaign={true}
        />
      );

    default:
      return null;
  }
};

export default MaterialCardRenderer;
