package org.aksw.sparqlify.qa.vocabularies;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * SIOC (Semantically-Interlinked Online Communities) is an ontology for
 * describing the information in online communities.
 * This information can be used to export information from online communities
 * and to link them together. The scope of the application areas that SIOC can
 * be used for includes (and is not limited to) weblogs, message boards, mailing
 * lists and chat channels.
 *
 * http://rdfs.org/sioc/ns#
 */
public class SIOC {
	
	private final static String sioc = "http://rdfs.org/sioc/ns#";
	
	/* classes */
	
	/** Community is a high-level concept that defines an online community and
	 * what it consists of.
	 */
	public final static Resource Community = ResourceFactory.createResource(sioc + "Community");
	/** An area in which content Items are contained. */
	public final static Resource Container = ResourceFactory.createResource(sioc + "Container");
	/** A discussion area on which Posts or entries are made. */
	public final static Resource Forum = ResourceFactory.createResource(sioc + "Forum");
	/** An Item is something which can be in a Container. */
	public final static Resource Item = ResourceFactory.createResource(sioc + "Item");
	/** An article or message that can be posted to a Forum. */
	public final static Resource Post = ResourceFactory.createResource(sioc + "Post");
	/** A Role is a function of a UserAccount within a scope of a particular
	 * Forum, Site, etc.
	 */
	public final static Resource Role = ResourceFactory.createResource(sioc  + "Role");
	/** A Space is a place where data resides, e.g. on a website, desktop, fileshare, etc. */
	public final static Resource Space = ResourceFactory.createResource(sioc + "Space");
	/** A Site can be the location of an online community or set of communities,
	 * with UserAccounts and Usergroups creating Items in a set of Containers.
	 * It can be thought of as a web-accessible data Space.
	 */
	public final static Resource Site = ResourceFactory.createResource(sioc + "Site");
	/** A container for a series of threaded discussion Posts or Items. */
	public final static Resource Thread = ResourceFactory.createResource(sioc + "Thread");
	/** A user account in an online community site. */
	public final static Resource UserAccount = ResourceFactory.createResource(sioc + "UserAccount");
	/** A set of UserAccounts whose owners have a common purpose or interest.
	 * Can be used for access control purposes.
	 */
	public final static Resource Usergroup = ResourceFactory.createResource(sioc + "Usergroup");
	
	/* properties */
	
	/** Specifies that this Item is about a particular resource, e.g. a Post
	 * describing a book, hotel, etc.
	 */
	public final static Property about = ResourceFactory.createProperty(sioc + "about");
	/** Refers to the foaf:Agent or foaf:Person who owns this sioc:UserAccount. */
	public final static Property account_of = ResourceFactory.createProperty(sioc + "account_of");
	/** Refers to who (e.g. a UserAccount, e-mail address, etc.) a particular
	 * Item is addressed to.
	 */
	public final static Property addressed_to =
			ResourceFactory.createProperty(sioc, "addressed_to");
	/** A Site that the UserAccount is an administrator of. */
	public final static Property administrator_of =
			ResourceFactory.createProperty(sioc, "administrator_of");
	/** The URI of a file attached to an Item. */
	public final static Property attachment =
			ResourceFactory.createProperty(sioc, "attachment");
	/** An image or depiction used to represent this UserAccount. */
	public final static Property avatar =
			ResourceFactory.createProperty(sioc, "avatar");
	/** An Item that this Container contains. */
	public final static Property container_of =
			ResourceFactory.createProperty(sioc, "container_of");
	/** The content of the Item in plain text format. */
	public final static Property content =
			ResourceFactory.createProperty(sioc, "content");
	/** A resource that the UserAccount is a creator of. */
	public final static Property creator_of =
			ResourceFactory.createProperty(sioc, "creator_of");
	/** Links to a previous (older) revision of this Item or Post. */
	public final static Property earlier_version =
			ResourceFactory.createProperty(sioc, "earlier_version");
	/** An electronic mail address of the UserAccount. */
	public final static Property email =
			ResourceFactory.createProperty(sioc, "email");
	/** An electronic mail address of the UserAccount, encoded using SHA1. */
	public final static Property email_sha1 =
			ResourceFactory.createProperty(sioc, "email_sha1");
	/** This links Items to embedded statements, facts and structured content. */
	public final static Property embeds_knowledge =
			ResourceFactory.createProperty(sioc, "embeds_knowledge");
	/** A feed (e.g. RSS, Atom, etc.) pertaining to this resource (e.g. for a
	 * Forum, Site, UserAccount, etc.).
	 */
	public final static Property feed =
			ResourceFactory.createProperty(sioc, "feed");
	/** Indicates that one UserAccount follows another UserAccount (e.g. for
	 * microblog posts or other content item updates).
	 */
	public final static Property follows =
			ResourceFactory.createProperty(sioc, "follows");
	/** A UserAccount that has this Role. */
	public final static Property function_of =
			ResourceFactory.createProperty(sioc, "function_of");
	/** A UserAccount that is an administrator of this Site. */
	public final static Property has_administrator =
			ResourceFactory.createProperty(sioc, "has_administrator");
	/** The Container to which this Item belongs. */
	public final static Property has_container =
			ResourceFactory.createProperty(sioc, "has_container");
	/** This is the UserAccount that made this resource. */
	public final static Property has_creator =
			ResourceFactory.createProperty(sioc, "has_creator");
	/** The discussion that is related to this Item. */
	public final static Property has_discussion =
			ResourceFactory.createProperty(sioc, "has_discussion");
	/** A Role that this UserAccount has. */
	public final static Property has_function =
			ResourceFactory.createProperty(sioc, "has_function");
	/** The Site that hosts this Forum. */
	public final static Property has_host =
			ResourceFactory.createProperty(sioc, "has_host");
	/** A UserAccount that is a member of this Usergroup. */
	public final static Property has_member =
			ResourceFactory.createProperty(sioc, "has_member");
	/** A UserAccount that is a moderator of this Forum. */
	public final static Property has_moderator =
			ResourceFactory.createProperty(sioc, "has_moderator");
	/** A UserAccount that modified this Item. */
	public final static Property has_modifier =
			ResourceFactory.createProperty(sioc, "has_modifier");
	/** A UserAccount that this resource is owned by. */
	public final static Property has_owner =
			ResourceFactory.createProperty(sioc, "has_owner");
	/** A Container or Forum that this Container or Forum is a child of. */
	public final static Property has_parent =
			ResourceFactory.createProperty(sioc, "has_parent");
	/** Points to an Item or Post that is a reply or response to this Item or Post. */
	public final static Property has_reply =
			ResourceFactory.createProperty(sioc, "has_reply");
	/** A resource that this Role applies to. */
	public final static Property has_scope =
			ResourceFactory.createProperty(sioc, "has_scope");
	/** A data Space which this resource is a part of. */
	public final static Property has_space =
			ResourceFactory.createProperty(sioc, "has_space");
	/** A UserAccount that is subscribed to this Container. */
	public final static Property has_subscriber =
			ResourceFactory.createProperty(sioc, "has_subscriber");
	/** Points to a Usergroup that has certain access to this Space. */
	public final static Property has_usergroup =
			ResourceFactory.createProperty(sioc, "has_usergroup");
	/** A Forum that is hosted on this Site. */
	public final static Property host_of =
			ResourceFactory.createProperty(sioc, "host_of");
	/** An identifier of a SIOC concept instance. For example, a user ID. Must
	 * be unique for instances of each type of SIOC concept within the same site.
	 */
	public final static Property id = ResourceFactory.createProperty(sioc, "id");
	/** The IP address used when creating this Item. This can be associated
	 * with a creator. Some wiki articles list the IP addresses for the creator
	 * or modifiers when the usernames are absent.
	 */
	public final static Property ip_address =
			ResourceFactory.createProperty(sioc, "ip_address");
	/** The date and time of the last activity associated with a SIOC concept
	 * instance, and expressed in ISO 8601 format. This could be due to a reply
	 * Post or Comment, a modification to an Item, etc.
	 */
	public final static Property last_activity_date =
			ResourceFactory.createProperty(sioc, "last_activity_date");
	/** The date and time of the last Post (or Item) in a Forum (or a
	 * Container), in ISO 8601 format.
	 */
	public final static Property last_item_date =
			ResourceFactory.createProperty(sioc, "last_item_date");
	/** The date and time of the last reply Post or Comment, which could be
	 * associated with a starter Item or Post or with a Thread, and expressed
	 * in ISO 8601 format.
	 */
	public final static Property last_reply_date =
			ResourceFactory.createProperty(sioc, "last_reply_date");
	/** Links to a later (newer) revision of this Item or Post. */
	public final static Property later_version =
			ResourceFactory.createProperty(sioc, "later_version");
	/** Links to the latest revision of this Item or Post. */
	public final static Property latest_version =
			ResourceFactory.createProperty(sioc, "latest_version");
	/** A URI of a document which contains this SIOC object. */
	public final static Property link = ResourceFactory.createProperty(sioc, "link");
	/** Links extracted from hyperlinks within a SIOC concept, e.g. Post or Site. */
	public final static Property links_to =
			ResourceFactory.createProperty(sioc, "links_to");
	/** A Usergroup that this UserAccount is a member of. */
	public final static Property member_of =
			ResourceFactory.createProperty(sioc, "member_of");
	/** A Forum that a UserAccount is a moderator of. */
	public final static Property moderator_of =
			ResourceFactory.createProperty(sioc, "moderator_of");
	/** An Item that this UserAccount has modified. */
	public final static Property modifier_of =
			ResourceFactory.createProperty(sioc, "modifier_of");
	/** The name of a SIOC concept instance, e.g. a username for a UserAccount,
	 * group name for a Usergroup, etc.
	 */
	public final static Property name = ResourceFactory.createProperty(sioc, "name");
	/** Next Item or Post in a given Container sorted by date */
	public final static Property next_by_date =
			ResourceFactory.createProperty(sioc, "next_by_date");
	/** Links to the next revision of this Item or Post. */
	public final static Property next_version =
			ResourceFactory.createProperty(sioc, "next_version");
	/** A note associated with this resource, for example, if it has been
	 * edited by a UserAccount. */
	public final static Property note =
			ResourceFactory.createProperty(sioc, "note");
	/** The number of unique authors (UserAccounts and unregistered posters)
	 * who have contributed to this Item, Thread, Post, etc.
	 */
	public final static Property num_authors =
			ResourceFactory.createProperty(sioc, "num_authors");
	/** The number of Posts (or Items) in a Forum (or a Container). */
	public final static Property num_items =
			ResourceFactory.createProperty(sioc, "num_items");
	/** The number of replies that this Item, Thread, Post, etc. has. Useful
	 * for when the reply structure is absent.
	 */
	public final static Property num_replies =
			ResourceFactory.createProperty(sioc, "num_replies");
	/** The number of Threads (AKA discussion topics) in a Forum. */
	public final static Property num_threads =
			ResourceFactory.createProperty(sioc, "num_threads");
	/** The number of times this Item, Thread, UserAccount profile, etc. has
	 * been viewed.
	 */
	public final static Property num_views =
			ResourceFactory.createProperty(sioc, "num_views");
	/** A resource owned by a particular UserAccount, for example, a weblog or
	 * image gallery.
	 */
	public final static Property owner_of =
			ResourceFactory.createProperty(sioc, "owner_of");
	/** A child Container or Forum that this Container or Forum is a parent of. */
	public final static Property parent_of =
			ResourceFactory.createProperty(sioc, "parent_of");
	/** Previous Item or Post in a given Container sorted by date. */
	public final static Property previous_by_date =
			ResourceFactory.createProperty(sioc, "previous_by_date");
	/** Links to the previous revision of this Item or Post. */
	public final static Property previous_version =
			ResourceFactory.createProperty(sioc, "previous_version");
	/** Related Posts for this Post, perhaps determined implicitly from topics
	 * or references.
	 */
	public final static Property related_to =
			ResourceFactory.createProperty(sioc, "related_to");
	/** Links to an Item or Post which this Item or Post is a reply to. */
	public final static Property reply_of =
			ResourceFactory.createProperty(sioc, "reply_of");
	/** A Role that has a scope of this resource. */
	public final static Property scope_of =
			ResourceFactory.createProperty(sioc, "scope_of");
	/** An Item may have a sibling or a twin that exists in a different
	 * Container, but the siblings may differ in some small way (for example,
	 * language, category, etc.). The sibling of this Item should be self
	 * describing (that is, it should contain all available information).
	 */
	public final static Property sibling =
			ResourceFactory.createProperty(sioc, "sibling");
	/** A resource which belongs to this data Space. */
	public final static Property space_of =
			ResourceFactory.createProperty(sioc, "space_of");
	/** A Container that a UserAccount is subscribed to. */
	public final static Property subscriber_of =
			ResourceFactory.createProperty(sioc, "subscriber_of");
	/** A topic of interest, linking to the appropriate URI, e.g. in the Open
	 * Directory Project or of a SKOS category.
	 */
	public final static Property topic =
			ResourceFactory.createProperty(sioc, "topic");
	/** A Space that the Usergroup has access to. */
	public final static Property usergroup_of =
			ResourceFactory.createProperty(sioc, "usergroup_of");
	
	/*
	 * deprecated
	 */
	/** UserAccount is now preferred. This is a deprecated class for a User in
	 * an online community site. (Deprecated)
	 */
	@Deprecated
	public final static Resource User =
			ResourceFactory.createResource(sioc + "User");
	/** This is the title (subject line) of the Post. Note that for a Post
	 * within a threaded discussion that has no parents, it would detail the
	 * topic thread.
	 */
	@Deprecated
	public final static Property title =
			ResourceFactory.createProperty(sioc, "title");
	/** The encoded content of the Post, contained in CDATA areas. */
	@Deprecated
	public final static Property content_encoded =
			ResourceFactory.createProperty(sioc, "content_encoded");
	/** When this was created, in ISO 8601 format. */
	@Deprecated
	public final static Property created_at =
			ResourceFactory.createProperty(sioc, "created_at");
	/** The content of the Post. */
	@Deprecated
	public final static Property description =
			ResourceFactory.createProperty(sioc, "description");
	/** First (real) name of this User. Synonyms include given name or
	 * christian name.
	 */
	@Deprecated
	public final static Property first_name =
			ResourceFactory.createProperty(sioc, "first_name");
	/** This property has been renamed. Use sioc:usergroup_of instead. */
	@Deprecated
	public final static Property group_of =
			ResourceFactory.createProperty(sioc, "group_of");
	/** This property has been renamed. Use sioc:has_usergroup instead. */
	@Deprecated
	public final static Property has_group =
			ResourceFactory.createProperty(sioc, "has_group");
	/** An resource that is a part of this subject. 
	 * This property is deprecated. Use dcterms:hasPart from the Dublin Core
	 * ontology instead.
	 */
	@Deprecated
	public final static Property has_part =
			ResourceFactory.createProperty(sioc, "has_part");
	/** Last (real) name of this user. Synonyms include surname or family name.
	 * This property is deprecated. Use foaf:name or foaf:surname from the FOAF
	 * vocabulary instead.
	 */
	@Deprecated
	public final static Property last_name =
			ResourceFactory.createProperty(sioc, "last_name");
	/** When this was modified, in ISO 8601 format.
	 * This property is deprecated. Use dcterms:modified from the Dublin Core
	 * ontology instead.
	 */
	@Deprecated
	public final static Property modified_at =
			ResourceFactory.createProperty(sioc, "modified_at");
	/** A resource that the subject is a part of.
	 * This property is deprecated. Use dcterms:isPartOf from the Dublin Core
	 * ontology instead.
	 */
	@Deprecated
	public final static Property part_of =
			ResourceFactory.createProperty(sioc, "part_of");
	/** Links either created explicitly or extracted implicitly on the HTML
	 * level from the Post.
	 * Renamed to sioc:links_to.
	 */
	@Deprecated
	public final static Property reference =
			ResourceFactory.createProperty(sioc, "reference");
	/** Keyword(s) describing subject of the Post.
	 * This property is deprecated. Use dcterms:subject from the Dublin Core
	 * ontology for text keywords and sioc:topic if the subject can be
	 * represented by a URI instead.
	 */
	@Deprecated
	public final static Property subject =
			ResourceFactory.createProperty(sioc, "subject");
}
